package com.zero.base.net

import android.content.Context
import com.drake.net.NetConfig
import com.drake.net.interceptor.LogRecordInterceptor
import com.drake.net.okhttp.setConverter
import com.drake.net.okhttp.setDebug
import com.drake.net.okhttp.setRequestInterceptor
import com.zero.base.data.IpManager
import com.zero.base.net.convert.GsonConverter
import com.zero.base.net.interceptor.GlobalParamInterceptor
import com.zero.base.net.interceptor.logInterceptor
import com.zero.library_base.BuildConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @date:2024/8/29 15:06
 * @path:com.zero.base.net.RetrofitInstance
 */
object RetrofitManager {
    /** 请求超时时间 */
    private const val TIME_OUT_SECONDS = 30L

    /** 请求根地址 */
    private val BASE_URL = IpManager.getDefaultIP()

    private lateinit var client: OkHttpClient

    fun initHttp(context: Context) {
        NetConfig.initialize(BASE_URL, context) {
            connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
            readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
            writeTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
            cache(Cache(context.cacheDir, 1024 * 1024 * 128))
            setDebug(BuildConfig.DEBUG)
            setRequestInterceptor(GlobalParamInterceptor())
            addInterceptor(LogRecordInterceptor(BuildConfig.DEBUG))
            setConverter(GsonConverter())
        }
        //后续弃用
        client = OkHttpClient.Builder().apply {
            addInterceptor(logInterceptor)
            connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
        }.build()
    }

    /**
     * Retrofit相关配置
     */
    fun <T> getService(serviceClass: Class<T>, baseUrl: String? = null): T {
        return Retrofit.Builder().client(client).addConverterFactory(GsonConverterFactory.create()).baseUrl(baseUrl
            ?: BASE_URL).build().create(serviceClass)
    }
}