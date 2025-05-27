package com.zero.base.net

import android.content.Context
import com.zero.base.data.IpManager
import com.zero.base.net.interceptor.logInterceptor
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
    private const val TIME_OUT_SECONDS = 10
    private lateinit var client: OkHttpClient

    /** 请求根地址 */
    private val BASE_URL = IpManager.getDefaultIP()

    fun initHttp(context: Context) {
        client = OkHttpClient.Builder()
            // 请求过滤器
            .addInterceptor(logInterceptor)
            // 请求超时时间
            .connectTimeout(TIME_OUT_SECONDS.toLong(), TimeUnit.SECONDS).build()
    }

    /**
     * Retrofit相关配置
     */
    fun <T> getService(serviceClass: Class<T>, baseUrl: String? = null): T {
        return Retrofit.Builder().client(client).addConverterFactory(GsonConverterFactory.create()).baseUrl(baseUrl
            ?: BASE_URL).build().create(serviceClass)
    }
}