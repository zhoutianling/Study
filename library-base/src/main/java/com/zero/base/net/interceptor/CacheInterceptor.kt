package com.zero.base.net.interceptor

import android.content.Context
import com.zero.base.util.NetworkUtil
import okhttp3.CacheControl
import okhttp3.Interceptor

/**
 * 缓存拦截器,用于无网情况下传递header直接拉取之前缓存的数据
 * @param day 缓存天数
 *
 */
class CacheInterceptor(private val context: Context, var day: Int = 7) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        if (!NetworkUtil.isNetworkAvailable(context)) {
            request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build()
        }
        val response = chain.proceed(request)
        if (!NetworkUtil.isNetworkAvailable(context)) {
            val maxAge = 60 * 60
            response.newBuilder().removeHeader("Pragma").header("Cache-Control", "public, max-age=$maxAge").build()
        } else {
            val maxStale = 60 * 60 * 24 * day
            response.newBuilder().removeHeader("Pragma").header("Cache-Control", "public, only-if-cached, max-stale=$maxStale").build()
        }
        return response
    }
}