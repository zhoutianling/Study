package com.zero.base.util

import android.content.Context
import android.view.Gravity
import android.widget.Toast

/**
 * Toast封装工具类
 */
object ToastUtil {

    /**
     * 显示短时间的Toast
     *
     * @param context Context
     * @param msg 显示的消息
     */
    fun showShort(context: Context, msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * 显示长时间的Toast
     *
     * @param context Context
     * @param msg 显示的消息
     */
    fun showLong(context: Context, msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_LONG).show()
    }

    /**
     * 居中显示短时间的Toast
     *
     * @param context Context
     * @param msg 显示的消息
     */
    fun showShortInCenter(context: Context, msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    /**
     * 居中显示短时间的Toast
     *
     * @param context Context
     * @param msg 显示的消息
     */
    fun showLongInCenter(context: Context, msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_LONG).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }
}