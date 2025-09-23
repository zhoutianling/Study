package com.zero.study

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.zero.study.provider.HookSwitchProvider.Companion.PATH_SWITCH
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
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
    }


    @SuppressLint("PrivateApi")
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != TARGET_PACKAGE) return
        XposedBridge.log("handleLoadPackage : ${lpparam.packageName} ")
        getHostContext {
            try {
                val hostName = it.packageManager.getApplicationLabel(it.applicationInfo)
                Log.d("zzz", "hostName: $hostName")
                getTargetContext()
                hookDeviceModel(lpparam, it)
                hookStatusCheck(lpparam, it)
                hookMethodParameters()
            } catch (e: Exception) {
                XposedBridge.log("handleLoadPackage Error: $e.message ")
            }
        }

    }

    /**
     * 获取宿主上下文
     */
    private fun getHostContext(block: (context: Context) -> Unit = {}) {
        XposedHelpers.findAndHookMethod(Application::class.java, "attach", Context::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val hostContext = param.args[0] as Context
                XposedBridge.log("getHostContext: ${hostContext.packageName}")
                block(hostContext)
            }
        })
    }

    /**
     * 获取目标上下文
     */
    private fun getTargetContext() {
        XposedHelpers.findAndHookMethod(Application::class.java, "attach", Context::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val hostContext = param.args[0] as Context
                try {
                    val moduleContext = hostContext.createPackageContext(TARGET_PACKAGE, Context.CONTEXT_IGNORE_SECURITY or Context.CONTEXT_INCLUDE_CODE)
                    val res = moduleContext.resources
                    val appName = res.getString(res.getIdentifier("app_name", "string", TARGET_PACKAGE))
                    Log.d("zzz", "getTargetContext: $moduleContext ")
                } catch (t: Throwable) {
                    Log.d("zzz", "getTargetContext Error: ${t.message}")
                }
            }
        })
    }

    // 1. Hook系统时间方法，将时间修改为特定值
    private fun hookMethodParameters() {
        try {
//            if (!isHookEnabled(context)) return
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

    // 2. Hook应用的getDeviceModel方法，修改设备型号
    private fun hookDeviceModel(lpparam: LoadPackageParam, context: Context) {
        try {
//            if (!isHookEnabled(context)) return
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
//            if (!isHookEnabled(context)) return
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


    private fun isHookEnabled(context: Context): Boolean {
        return try {
            // 使用应用的包名构建正确的 URI
            val appContext = context.applicationContext
            val authority = "${appContext.packageName}.HookSwitchProvider"
            val uri = "content://$authority/$PATH_SWITCH".toUri()
            Log.d("zzz", "Querying URI: $uri")
            val contentResolver: ContentResolver = context.contentResolver
            val cursor: Cursor? = contentResolver.query(uri, arrayOf("is_enabled"), null, null, null)
            var enabled = false
            cursor?.use {
                if (it.count > 0 && it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex("is_enabled")
                    if (columnIndex != -1) {
                        val value = it.getInt(columnIndex)
                        enabled = value == 1
                        Log.d("zzz", "Hook enabled value from provider: $enabled")
                    }
                }
            }
            enabled

        } catch (e: Exception) {
            false
        }
    }
}