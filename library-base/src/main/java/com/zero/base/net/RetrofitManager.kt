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
import com.zero.library_base.BuildConfig
import okhttp3.Cache
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


    fun initHttp(context: Context) {
        NetConfig.initialize(BASE_URL, context) {
            connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
            readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
            writeTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
            cache(Cache(context.cacheDir, 1024 * 1024 * 64))
            setDebug(BuildConfig.DEBUG)
            setRequestInterceptor(GlobalParamInterceptor())

            addInterceptor(LogRecordInterceptor(BuildConfig.DEBUG))
            setConverter(GsonConverter())
        }
    }
}