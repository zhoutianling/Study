
package com.drake.net.cache

import com.drake.net.request.tagOf
import com.drake.net.tag.NetTag
import com.drake.net.utils.OkHttpUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.internal.EMPTY_HEADERS
import okhttp3.internal.cache.CacheRequest
import okhttp3.internal.cache.DiskLruCache
import okhttp3.internal.closeQuietly
import okhttp3.internal.discard
import okhttp3.internal.http.ExchangeCodec
import okhttp3.internal.http.RealResponseBody
import okhttp3.internal.http.StatusLine
import okhttp3.internal.platform.Platform
import okhttp3.internal.toLongOrDefault
import okio.*
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import java.io.Closeable
import java.io.File
import java.io.Flushable
import java.io.IOException
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Caches HTTP and HTTPS responses to the filesystem so they may be reused, saving time and
 * bandwidth.
 *
 * ## Cache Optimization
 *
 * To measure cache effectiveness, this class tracks three statistics:
 *
 *  * **[Request Count:][requestCount]** the number of HTTP requests issued since this cache was
 *    created.
 *  * **[Network Count:][networkCount]** the number of those requests that required network use.
 *  * **[Hit Count:][hitCount]** the number of those requests whose responses were served by the
 *    cache.
 *
 * Sometimes a request will result in a conditional cache hit. If the cache contains a stale copy of
 * the response, the client will issue a conditional `GET`. The server will then send either
 * the updated response if it has changed, or a short 'not modified' response if the client's copy
 * is still valid. Such responses increment both the network count and hit count.
 *
 * The best way to improve the cache hit rate is by configuring the web server to return cacheable
 * responses. Although this client honors all [HTTP/1.1 (RFC 7234)][rfc_7234] cache headers, it
 * doesn't cache partial responses.
 *
 * ## Force a Network Response
 *
 * In some situations, such as after a user clicks a 'refresh' button, it may be necessary to skip
 * the cache, and fetch data directly from the server. To force a full refresh, add the `no-cache`
 * directive:
 *
 * ```
 * Request request = new Request.Builder()
 *     .cacheControl(new CacheControl.Builder().noCache().build())
 *     .url("http://publicobject.com/helloworld.txt")
 *     .build();
 * ```
 *
 * If it is only necessary to force a cached response to be validated by the server, use the more
 * efficient `max-age=0` directive instead:
 *
 * ```
 * Request request = new Request.Builder()
 *     .cacheControl(new CacheControl.Builder()
 *         .maxAge(0, TimeUnit.SECONDS)
 *         .build())
 *     .url("http://publicobject.com/helloworld.txt")
 *     .build();
 * ```
 *
 * ## Force a Cache Response
 *
 * Sometimes you'll want to show resources if they are available immediately, but not otherwise.
 * This can be used so your application can show *something* while waiting for the latest data to be
 * downloaded. To restrict a request to locally-cached resources, add the `only-if-cached`
 * directive:
 *
 * ```
 * Request request = new Request.Builder()
 *     .cacheControl(new CacheControl.Builder()
 *         .onlyIfCached()
 *         .build())
 *     .url("http://publicobject.com/helloworld.txt")
 *     .build();
 * Response forceCacheResponse = client.newCall(request).execute();
 * if (forceCacheResponse.code() != 504) {
 *   // The resource was cached! Show it.
 * } else {
 *   // The resource was not cached.
 * }
 * ```
 *
 * This technique works even better in situations where a stale response is better than no response.
 * To permit stale cached responses, use the `max-stale` directive with the maximum staleness in
 * seconds:
 *
 * ```
 * Request request = new Request.Builder()
 *     .cacheControl(new CacheControl.Builder()
 *         .maxStale(365, TimeUnit.DAYS)
 *         .build())
 *     .url("http://publicobject.com/helloworld.txt")
 *     .build();
 * ```
 *
 * The [CacheControl] class can configure request caching directives and parse response caching
 * directives. It even offers convenient constants [CacheControl.FORCE_NETWORK] and
 * [CacheControl.FORCE_CACHE] that address the use cases above.
 *
 * [rfc_7234]: http://tools.ietf.org/html/rfc7234
 */
class ForceCache internal constructor(
    val cache: DiskLruCache
) : Closeable, Flushable {

    // read and write statistics, all guarded by 'this'.
    internal var writeSuccessCount = 0
    internal var writeAbortCount = 0

    val isClosed: Boolean
        get() = cache.isClosed()

    internal fun get(request: Request): Response? {
        val snapshot: DiskLruCache.Snapshot = try {
            cache[key(request)] ?: return null
        } catch (_: IOException) {
            return null // Give up because the cache cannot be read.
        }

        val entry: Entry = try {
            Entry(snapshot.getSource(ENTRY_METADATA))
        } catch (_: IOException) {
            snapshot.closeQuietly()
            return null
        }

        val response = entry.response(snapshot, request.body)
        val value = request.tagOf<NetTag.CacheValidTime>()?.value
        return if (value != null && System.currentTimeMillis() - response.receivedResponseAtMillis > value) {
            null
        } else {
            response.newBuilder().request(request).build()
        }
    }

    internal fun put(response: Response): Response {
        if (!response.isSuccessful) return response
        val entry = Entry(response)
        var editor: DiskLruCache.Editor? = null
        val cacheRequest: CacheRequest? = try {
            editor = cache.edit(key(response.request)) ?: return response
            entry.writeTo(editor)
            RealCacheRequest(editor)
        } catch (_: IOException) {
            abortQuietly(editor)
            null
        }
        // Some apps return a null body; for compatibility we treat that like a null cache request.
        cacheRequest ?: return response
        val cacheBody = cacheRequest.body().buffer()
        val responseBody = response.body ?: return response
        val source = responseBody.source()
        val cacheWritingSource = object : Source {
            private var cacheRequestClosed = false

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead: Long
                try {
                    bytesRead = source.read(sink, byteCount)
                } catch (e: IOException) {
                    if (!cacheRequestClosed) {
                        cacheRequestClosed = true
                        cacheRequest.abort() // Failed to write a complete cache response.
                    }
                    throw e
                }

                if (bytesRead == -1L) {
                    if (!cacheRequestClosed) {
                        cacheRequestClosed = true
                        cacheBody.close() // The cache response is complete!
                    }
                    return -1
                }

                sink.copyTo(cacheBody.buffer, sink.size - bytesRead, bytesRead)
                cacheBody.emitCompleteSegments()
                return bytesRead
            }

            override fun timeout() = source.timeout()

            @Throws(IOException::class)
            override fun close() {
                if (!cacheRequestClosed &&
                    !discard(ExchangeCodec.DISCARD_STREAM_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                    cacheRequestClosed = true
                    cacheRequest.abort()
                }
                source.close()
            }
        }
        val contentType = response.header("Content-Type")
        val contentLength = responseBody.contentLength()
        return response.newBuilder()
            .body(RealResponseBody(contentType, contentLength, cacheWritingSource.buffer()))
            .build()
    }

    @Throws(IOException::class)
    internal fun remove(request: Request) {
        cache.remove(key(request))
    }

    internal fun update(cached: Response, network: Response) {
        val entry = Entry(network)
        val snapshot = (cached.body as CacheResponseBody).snapshot
        var editor: DiskLruCache.Editor? = null
        try {
            editor = snapshot.edit() ?: return // edit() returns null if snapshot is not current.
            entry.writeTo(editor)
            editor.commit()
        } catch (_: IOException) {
            abortQuietly(editor)
        }
    }

    private fun abortQuietly(editor: DiskLruCache.Editor?) {
        // Give up because the cache cannot be written.
        try {
            editor?.abort()
        } catch (_: IOException) {
        }
    }

    /**
     * Initialize the cache. This will include reading the journal files from the storage and building
     * up the necessary in-memory cache information.
     *
     * The initialization time may vary depending on the journal file size and the current actual
     * cache size. The application needs to be aware of calling this function during the
     * initialization phase and preferably in a background worker thread.
     *
     * Note that if the application chooses to not call this method to initialize the cache. By
     * default, OkHttp will perform lazy initialization upon the first usage of the cache.
     */
    @Throws(IOException::class)
    fun initialize() {
        cache.initialize()
    }

    /**
     * Closes the cache and deletes all of its stored values. This will delete all files in the cache
     * directory including files that weren't created by the cache.
     */
    @Throws(IOException::class)
    fun delete() {
        cache.delete()
    }

    /**
     * Deletes all values stored in the cache. In-flight writes to the cache will complete normally,
     * but the corresponding responses will not be stored.
     */
    @Throws(IOException::class)
    fun evictAll() {
        cache.evictAll()
    }

    /**
     * Returns an iterator over the URLs in this cache. This iterator doesn't throw
     * `ConcurrentModificationException`, but if new responses are added while iterating, their URLs
     * will not be returned. If existing responses are evicted during iteration, they will be absent
     * (unless they were already returned).
     *
     * The iterator supports [MutableIterator.remove]. Removing a URL from the iterator evicts the
     * corresponding response from the cache. Use this to evict selected responses.
     */
    @Throws(IOException::class)
    fun urls(): MutableIterator<String> {
        return object : MutableIterator<String> {
            private val delegate: MutableIterator<DiskLruCache.Snapshot> = cache.snapshots()
            private var nextUrl: String? = null
            private var canRemove = false

            override fun hasNext(): Boolean {
                if (nextUrl != null) return true

                canRemove = false // Prevent delegate.remove() on the wrong item!
                while (delegate.hasNext()) {
                    try {
                        delegate.next().use { snapshot ->
                            val metadata = snapshot.getSource(ENTRY_METADATA).buffer()
                            nextUrl = metadata.readUtf8LineStrict()
                            return true
                        }
                    } catch (_: IOException) {
                        // We couldn't read the metadata for this snapshot; possibly because the host filesystem
                        // has disappeared! Skip it.
                    }
                }

                return false
            }

            override fun next(): String {
                if (!hasNext()) throw NoSuchElementException()
                val result = nextUrl!!
                nextUrl = null
                canRemove = true
                return result
            }

            override fun remove() {
                check(canRemove) { "remove() before next()" }
                delegate.remove()
            }
        }
    }

    @Synchronized
    fun writeAbortCount(): Int = writeAbortCount

    @Synchronized
    fun writeSuccessCount(): Int = writeSuccessCount

    @Throws(IOException::class)
    fun size(): Long = cache.size()

    /** Max size of the cache (in bytes). */
    fun maxSize(): Long = cache.maxSize

    @Throws(IOException::class)
    override fun flush() {
        cache.flush()
    }

    @Throws(IOException::class)
    override fun close() {
        cache.close()
    }

    @get:JvmName("directory")
    val directory: File
        get() = cache.directory

    private inner class RealCacheRequest(
        private val editor: DiskLruCache.Editor
    ) : CacheRequest {
        private val cacheOut: Sink = editor.newSink(ENTRY_BODY)
        private val body: Sink
        var done = false

        init {
            this.body = object : ForwardingSink(cacheOut) {
                @Throws(IOException::class)
                override fun close() {
                    synchronized(this@ForceCache) {
                        if (done) return
                        done = true
                        writeSuccessCount++
                    }
                    super.close()
                    editor.commit()
                }
            }
        }

        override fun abort() {
            synchronized(this@ForceCache) {
                if (done) return
                done = true
                writeAbortCount++
            }
            cacheOut.closeQuietly()
            try {
                editor.abort()
            } catch (_: IOException) {
            }
        }

        override fun body(): Sink = body
    }

    private class Entry {
        private val url: String
        private val varyHeaders: Headers
        private val requestMethod: String
        private val protocol: Protocol
        private val code: Int
        private val message: String
        private val responseHeaders: Headers
        private val handshake: Handshake?
        private val sentRequestMillis: Long
        private val receivedResponseMillis: Long

        private val isHttps: Boolean get() = url.startsWith("https://")

        /**
         * Reads an entry from an input stream. A typical entry looks like this:
         *
         * ```
         * http://google.com/foo
         * GET
         * 2
         * Accept-Language: fr-CA
         * Accept-Charset: UTF-8
         * HTTP/1.1 200 OK
         * 3
         * Content-Type: image/png
         * Content-Length: 100
         * Cache-Control: max-age=600
         * ```
         *
         * A typical HTTPS file looks like this:
         *
         * ```
         * https://google.com/foo
         * GET
         * 2
         * Accept-Language: fr-CA
         * Accept-Charset: UTF-8
         * HTTP/1.1 200 OK
         * 3
         * Content-Type: image/png
         * Content-Length: 100
         * Cache-Control: max-age=600
         *
         * AES_256_WITH_MD5
         * 2
         * base64-encoded peerCertificate[0]
         * base64-encoded peerCertificate[1]
         * -1
         * TLSv1.2
         * ```
         *
         * The file is newline separated. The first two lines are the URL and the request method. Next
         * is the number of HTTP Vary request header lines, followed by those lines.
         *
         * Next is the response status line, followed by the number of HTTP response header lines,
         * followed by those lines.
         *
         * HTTPS responses also contain SSL session information. This begins with a blank line, and then
         * a line containing the cipher suite. Next is the length of the peer certificate chain. These
         * certificates are base64-encoded and appear each on their own line. The next line contains the
         * length of the local certificate chain. These certificates are also base64-encoded and appear
         * each on their own line. A length of -1 is used to encode a null array. The last line is
         * optional. If present, it contains the TLS version.
         */
        @Throws(IOException::class)
        constructor(rawSource: Source) {
            try {
                val source = rawSource.buffer()
                url = source.readUtf8LineStrict()
                requestMethod = source.readUtf8LineStrict()
                val varyHeadersBuilder = Headers.Builder()
                val varyRequestHeaderLineCount = readInt(source)
                for (i in 0 until varyRequestHeaderLineCount) {
                    OkHttpUtils.addLenient(varyHeadersBuilder, source.readUtf8LineStrict())
                }
                varyHeaders = varyHeadersBuilder.build()

                val statusLine = StatusLine.parse(source.readUtf8LineStrict())
                protocol = statusLine.protocol
                code = statusLine.code
                message = statusLine.message
                val responseHeadersBuilder = Headers.Builder()
                val responseHeaderLineCount = readInt(source)
                for (i in 0 until responseHeaderLineCount) {
                    OkHttpUtils.addLenient(responseHeadersBuilder, source.readUtf8LineStrict())
                }
                val sendRequestMillisString = responseHeadersBuilder[SENT_MILLIS]
                val receivedResponseMillisString = responseHeadersBuilder[RECEIVED_MILLIS]
                responseHeadersBuilder.removeAll(SENT_MILLIS)
                responseHeadersBuilder.removeAll(RECEIVED_MILLIS)
                sentRequestMillis = sendRequestMillisString?.toLong() ?: 0L
                receivedResponseMillis = receivedResponseMillisString?.toLong() ?: 0L
                responseHeaders = responseHeadersBuilder.build()

                if (isHttps) {
                    val blank = source.readUtf8LineStrict()
                    if (blank.isNotEmpty()) {
                        throw IOException("expected \"\" but was \"$blank\"")
                    }
                    val cipherSuiteString = source.readUtf8LineStrict()
                    val cipherSuite = CipherSuite.forJavaName(cipherSuiteString)
                    val peerCertificates = readCertificateList(source)
                    val localCertificates = readCertificateList(source)
                    val tlsVersion = if (!source.exhausted()) {
                        TlsVersion.forJavaName(source.readUtf8LineStrict())
                    } else {
                        TlsVersion.SSL_3_0
                    }
                    handshake = Handshake.get(tlsVersion, cipherSuite, peerCertificates, localCertificates)
                } else {
                    handshake = null
                }
            } finally {
                rawSource.close()
            }
        }

        constructor(response: Response) {
            this.url = response.request.url.toString()
            this.varyHeaders = response.varyHeaders()
            this.requestMethod = response.request.method
            this.protocol = response.protocol
            this.code = response.code
            this.message = response.message
            this.responseHeaders = response.headers
            this.handshake = response.handshake
            this.sentRequestMillis = response.sentRequestAtMillis
            this.receivedResponseMillis = response.receivedResponseAtMillis
        }

        @Throws(IOException::class)
        fun writeTo(editor: DiskLruCache.Editor) {
            editor.newSink(ENTRY_METADATA).buffer().use { sink ->
                sink.writeUtf8(url).writeByte('\n'.toInt())
                sink.writeUtf8(requestMethod).writeByte('\n'.toInt())
                sink.writeDecimalLong(varyHeaders.size.toLong()).writeByte('\n'.toInt())
                for (i in 0 until varyHeaders.size) {
                    sink.writeUtf8(varyHeaders.name(i))
                        .writeUtf8(": ")
                        .writeUtf8(varyHeaders.value(i))
                        .writeByte('\n'.toInt())
                }

                sink.writeUtf8(StatusLine(protocol, code, message).toString()).writeByte('\n'.toInt())
                sink.writeDecimalLong((responseHeaders.size + 2).toLong()).writeByte('\n'.toInt())
                for (i in 0 until responseHeaders.size) {
                    sink.writeUtf8(responseHeaders.name(i))
                        .writeUtf8(": ")
                        .writeUtf8(responseHeaders.value(i))
                        .writeByte('\n'.toInt())
                }
                sink.writeUtf8(SENT_MILLIS)
                    .writeUtf8(": ")
                    .writeDecimalLong(sentRequestMillis)
                    .writeByte('\n'.toInt())
                sink.writeUtf8(RECEIVED_MILLIS)
                    .writeUtf8(": ")
                    .writeDecimalLong(receivedResponseMillis)
                    .writeByte('\n'.toInt())

                if (isHttps) {
                    sink.writeByte('\n'.toInt())
                    sink.writeUtf8(handshake!!.cipherSuite.javaName).writeByte('\n'.toInt())
                    writeCertList(sink, handshake.peerCertificates)
                    writeCertList(sink, handshake.localCertificates)
                    sink.writeUtf8(handshake.tlsVersion.javaName).writeByte('\n'.toInt())
                }
            }
        }

        @Throws(IOException::class)
        private fun readCertificateList(source: BufferedSource): List<Certificate> {
            val length = readInt(source)
            if (length == -1) return emptyList() // OkHttp v1.2 used -1 to indicate null.

            try {
                val certificateFactory = CertificateFactory.getInstance("X.509")
                val result = ArrayList<Certificate>(length)
                for (i in 0 until length) {
                    val line = source.readUtf8LineStrict()
                    val bytes = Buffer()
                    bytes.write(line.decodeBase64()!!)
                    result.add(certificateFactory.generateCertificate(bytes.inputStream()))
                }
                return result
            } catch (e: CertificateException) {
                throw IOException(e.message)
            }
        }

        @Throws(IOException::class)
        private fun writeCertList(sink: BufferedSink, certificates: List<Certificate>) {
            try {
                sink.writeDecimalLong(certificates.size.toLong()).writeByte('\n'.toInt())
                for (element in certificates) {
                    val bytes = element.encoded
                    val line = bytes.toByteString().base64()
                    sink.writeUtf8(line).writeByte('\n'.toInt())
                }
            } catch (e: CertificateEncodingException) {
                throw IOException(e.message)
            }
        }

        fun response(snapshot: DiskLruCache.Snapshot, requestBody: RequestBody?): Response {
            val contentType = responseHeaders["Content-Type"]
            val contentLength = responseHeaders["Content-Length"]
            val cacheRequest = Request.Builder()
                .url(url)
                .method(requestMethod, requestBody)
                .headers(varyHeaders)
                .build()
            val builder = Response.Builder()
                .request(cacheRequest)
                .protocol(protocol)
                .code(code)
                .message(message)
                .headers(responseHeaders)
                .handshake(handshake)
                .sentRequestAtMillis(sentRequestMillis)
                .receivedResponseAtMillis(receivedResponseMillis)
            return builder
                .cacheResponse(builder.build())
                .body(CacheResponseBody(snapshot, contentType, contentLength))
                .build()
        }

        companion object {
            /** Synthetic response header: the local time when the request was sent. */
            private val SENT_MILLIS = "${Platform.get().getPrefix()}-Sent-Millis"

            /** Synthetic response header: the local time when the response was received. */
            private val RECEIVED_MILLIS = "${Platform.get().getPrefix()}-Received-Millis"
        }
    }

    private class CacheResponseBody(
        val snapshot: DiskLruCache.Snapshot,
        private val contentType: String?,
        private val contentLength: String?
    ) : ResponseBody() {
        private val bodySource: BufferedSource

        init {
            val source = snapshot.getSource(ENTRY_BODY)
            bodySource = object : ForwardingSource(source) {
                @Throws(IOException::class)
                override fun close() {
                    snapshot.close()
                    super.close()
                }
            }.buffer()
        }

        override fun contentType(): MediaType? = contentType?.toMediaTypeOrNull()

        override fun contentLength(): Long = contentLength?.toLongOrDefault(-1L) ?: -1L

        override fun source(): BufferedSource = bodySource
    }

    companion object {
        private const val ENTRY_METADATA = 0
        private const val ENTRY_BODY = 1

        @JvmStatic
        fun key(request: Request): String {
            val key = request.tagOf<NetTag.CacheKey>()?.value ?: (request.method + request.url.toString())
            return key.encodeUtf8().sha1().hex()
        }

        @Throws(IOException::class)
        internal fun readInt(source: BufferedSource): Int {
            try {
                val result = source.readDecimalLong()
                val line = source.readUtf8LineStrict()
                if (result < 0L || result > Integer.MAX_VALUE || line.isNotEmpty()) {
                    throw IOException("expected an int but was \"$result$line\"")
                }
                return result.toInt()
            } catch (e: NumberFormatException) {
                throw IOException(e.message)
            }
        }

        /**
         * Returns true if none of the Vary headers have changed between [cachedRequest] and
         * [newRequest].
         */
        fun varyMatches(
            cachedResponse: Response,
            cachedRequest: Headers,
            newRequest: Request
        ): Boolean {
            return cachedResponse.headers.varyFields().none {
                cachedRequest.values(it) != newRequest.headers(it)
            }
        }

        /** Returns true if a Vary header contains an asterisk. Such responses cannot be cached. */
        fun Response.hasVaryAll() = "*" in headers.varyFields()

        /**
         * Returns the names of the request headers that need to be checked for equality when caching.
         */
        private fun Headers.varyFields(): Set<String> {
            var result: MutableSet<String>? = null
            for (i in 0 until size) {
                if (!"Vary".equals(name(i), ignoreCase = true)) {
                    continue
                }

                val value = value(i)
                if (result == null) {
                    result = TreeSet(String.CASE_INSENSITIVE_ORDER)
                }
                for (varyField in value.split(',')) {
                    result.add(varyField.trim())
                }
            }
            return result ?: emptySet()
        }

        /**
         * Returns the subset of the headers in this's request that impact the content of this's body.
         */
        fun Response.varyHeaders(): Headers {
            // Use the request headers sent over the network, since that's what the response varies on.
            // Otherwise OkHttp-supplied headers like "Accept-Encoding: gzip" may be lost.
            val requestHeaders = networkResponse!!.request.headers
            val responseHeaders = headers
            return varyHeaders(requestHeaders, responseHeaders)
        }

        /**
         * Returns the subset of the headers in [requestHeaders] that impact the content of the
         * response's body.
         */
        private fun varyHeaders(requestHeaders: Headers, responseHeaders: Headers): Headers {
            val varyFields = responseHeaders.varyFields()
            if (varyFields.isEmpty()) return EMPTY_HEADERS

            val result = Headers.Builder()
            for (i in 0 until requestHeaders.size) {
                val fieldName = requestHeaders.name(i)
                if (fieldName in varyFields) {
                    result.add(fieldName, requestHeaders.value(i))
                }
            }
            return result.build()
        }
    }
}
