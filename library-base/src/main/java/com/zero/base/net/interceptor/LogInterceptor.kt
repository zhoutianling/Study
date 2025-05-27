package com.zero.base.net.interceptor

import android.util.Log
import com.zero.library_base.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor


/**
 * okhttp 日志拦截器
 * @author LTP  2022/3/21
 */
val logInterceptor = HttpLoggingInterceptor { message ->
    Log.d("net_log", message)
}.setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC)