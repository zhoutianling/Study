package com.zero.base.ext

import androidx.lifecycle.viewModelScope
import com.zero.base.model.BaseViewModel
import com.zero.base.net.bean.ApiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * BaseViewModel的一些扩展方法
 *
 */

/**
 * 启动协程，封装了viewModelScope.launch
 *
 * @param tryBlock try语句运行的函数
 * @param catchBlock catch语句运行的函数，可以用来做一些网络异常等的处理，默认空实现
 * @param finallyBlock finally语句运行的函数，可以用来做一些资源回收等，默认空实现
 */
fun BaseViewModel.launch(tryBlock: suspend CoroutineScope.() -> Unit, catchBlock: suspend CoroutineScope.() -> Unit = {}, finallyBlock: suspend CoroutineScope.() -> Unit = {}) {
    // 默认是执行在主线程，相当于launch(Dispatchers.Main)
    viewModelScope.launch {
        try {
            tryBlock()
        } catch (e: Exception) {
            exception.value = e
            catchBlock()
        } finally {
            finallyBlock()
        }
    }
}

/**
 * 请求结果处理
 *
 * @param response ApiResponse
 * @param successBlock 服务器请求成功返回成功码的执行回调，默认空实现
 * @param errorBlock 服务器请求成功返回错误码的执行回调，默认返回false的空实现，函数返回值true:拦截统一错误处理，false:不拦截
 */
fun <T> BaseViewModel.handleRequest(response: ApiResponse<T>, successBlock: suspend CoroutineScope.(response: ApiResponse<T>) -> Unit = {}, errorBlock: suspend CoroutineScope.(response: ApiResponse<T>) -> Boolean = { false }) {
    viewModelScope.launch {
        when (response.errorCode) {
            0 -> successBlock(response) // 服务器返回请求成功码
            else -> { // 服务器返回的其他错误码
                if (!errorBlock(response)) {
                    // 只有errorBlock返回false不拦截处理时，才去统一提醒错误提示
                    errorResponse.value = response
                }
            }
        }
    }
}

/**
 * 请求结果处理(使用协程二次封装，可避免魔鬼嵌套)
 *
 * @param response ApiResponse
 * @param handleErrorSelf 是否拦截全局的网络错误处理，默认false
 * @return Result<ApiResponse>
 */
suspend fun <T> BaseViewModel.handleRequest(response: ApiResponse<T>, handleErrorSelf: Boolean = false): Result<ApiResponse<T>> {
    return suspendCancellableCoroutine { continuation ->
        when (response.errorCode) {
            0 -> {
                continuation.resume(Result.success(response))
            }

            else -> { // 服务器返回的其他错误码
                if (!handleErrorSelf) {
                    // 只有errorBlock返回false不拦截处理时，才去统一提醒错误提示
                    errorResponse.value = response
                }
                continuation.resume(Result.failure(Exception(response.errorMsg)))
            }
        }
    }
}