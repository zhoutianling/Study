

package com.drake.net.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


// <editor-fold desc="切换调度器">

/**
 * 切换到主线程调度器
 */
suspend fun <T> withMain(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Main, block)

/**
 * 切换到IO程调度器
 */
suspend fun <T> withIO(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.IO, block)

/**
 * 切换到默认调度器
 */
suspend fun <T> withDefault(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Default, block)

/**
 * 切换到没有限制的调度器
 */
suspend fun <T> withUnconfined(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Unconfined, block)

// </editor-fold>

/**
 * 在主线程运行
 */
private val mainThreadHandler by lazy { Handler(Looper.getMainLooper()) }
fun runMain(block: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        block()
    } else {
        mainThreadHandler.post { block() }
    }
}