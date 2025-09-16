package com.zero.study

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import com.zero.study.provider.HookSwitchProvider.Companion.AUTHORITY
import com.zero.study.provider.HookSwitchProvider.Companion.PATH_SWITCH
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @date:2025/9/11 14:27
 * @path:com.zero.study.HookMain
 */
class HookMain : IXposedHookLoadPackage {
    companion object {
        private const val TARGET_PACKAGE = "com.zero.emptykotlin"
        private val HOOK_SWITCH_URI = "content://$AUTHORITY/$PATH_SWITCH".toUri()
    }

    @SuppressLint("PrivateApi")
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != TARGET_PACKAGE) return
        Log.d("zzz", "handleLoadPackage: ${lpparam.packageName}")
        try {
            val activityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader)
            XposedHelpers.findAndHookMethod(activityClass, "onCreate", Bundle::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val context = param.thisObject as Context
                    Log.d("zzz", "context: $context")
                    hookDeviceModel(lpparam, context)
                    hookStatusCheck(lpparam, context)
                    hookMethodParameters(lpparam, context)
                }
            })

        } catch (e: Exception) {
            Log.d("zzz", "handleLoadPackage Error: ${e.message}")
            e.printStackTrace()
        }
    }

    // 1. Hook系统时间方法，将时间修改为特定值
    private fun hookSystemTime(lpparam: LoadPackageParam) {
        try {
            val dateClass = Date::class.java
            XposedHelpers.findAndHookConstructor(dateClass, Long::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // 将所有Date对象的时间改为2023年10月1日
                    val targetTime = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse("2023-10-01")?.time
                        ?: 0
                    (param.thisObject as Date).time = targetTime
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 2. Hook应用的getDeviceModel方法，修改设备型号
    private fun hookDeviceModel(lpparam: LoadPackageParam, context: Context) {
        try {
            if (!isHookEnabled(context)) return
            Log.d("zzz", "hookDeviceModel ")
            val mainActivityClass = XposedHelpers.findClass("com.zero.emptykotlin.MainActivity", lpparam.classLoader)
            XposedHelpers.findAndHookMethod(mainActivityClass, "getDeviceModel", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // 将设备型号改为自定义值
                    param.result = "Xposed Custom Device"
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 3. Hook应用的checkStatus方法，强制返回"正常"
    private fun hookStatusCheck(lpparam: LoadPackageParam, context: Context) {
        try {
            if (!isHookEnabled(context)) return
            Log.d("zzz", "hookStatusCheck ")
            val mainActivityClass = XposedHelpers.findClass("com.zero.emptykotlin.MainActivity", lpparam.classLoader)
            XposedHelpers.findAndHookMethod(mainActivityClass, "checkStatus", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    // 在方法执行前直接返回结果，跳过原方法执行
                    param.result = "已被Xposed优化 - 正常"
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 4. 演示如何拦截并修改方法参数
    private fun hookMethodParameters(lpparam: LoadPackageParam, context: Context) {
        try {
            if (!isHookEnabled(context)) return
            Log.d("zzz", "hookMethodParameters ")
            val simpleDateFormatClass = SimpleDateFormat::class.java
            XposedHelpers.findAndHookConstructor(simpleDateFormatClass, String::class.java, Locale::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    // 修改日期格式参数
                    val pattern = param.args[0] as String
                    if (pattern.contains("yyyy-MM-dd")) {
                        param.args[0] = "yyyy年MM月dd日 HH:mm:ss"
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isHookEnabled(context: Context): Boolean {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            Log.d("zzz", "Querying URI: $HOOK_SWITCH_URI")
            val cursor: Cursor? = contentResolver.query(HOOK_SWITCH_URI, arrayOf("is_enabled"), null, null, null)
            var enabled = false
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex("is_enabled")
                    Log.d("zzz", "columnIndex: $columnIndex")
                    if (columnIndex != -1) {
                        val value = it.getInt(columnIndex)
                        enabled = value == 1
                        Log.d("zzz", "Hook enabled value from provider: $value")
                    } else {
                        Log.e("zzz", "Column 'is_enabled' not found in cursor")
                    }
                } else {
                    Log.e("zzz", "Cursor is empty")
                }
            }
            Log.d("zzz", "isHookEnabled result: $enabled")
            enabled

        } catch (e: Exception) {
            Log.e("zzz", "isHookEnabled error: ${e.message}", e)
            false
        }
    }
}