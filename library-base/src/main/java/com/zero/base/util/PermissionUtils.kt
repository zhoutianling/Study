package com.zero.base.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * @date:2024/7/11 10:37
 * @path:com.zero.study.util.PermissionUtils
 */
object PermissionUtils {
    /**
     * 打开显示在其他应用上层权限
     */
    fun applyOverlay(activity: Activity): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.data = Uri.parse("package:${activity.packageName}")
            ContextCompat.startActivity(activity, intent, null)
            true
        } catch (e: Exception) {
            e.printStackTrace();
            false
        }
    }

    /**
     * 一键打开文件管理页面
     *  需要添加MANAGE_EXTERNAL_STORAGE 特殊权限和传入当前应用包名
     */
    fun appFileManager(context: Context): Boolean {
        return try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            } else {
                Intent(Settings.ACTION_SETTINGS)
            }
            intent.data = Uri.parse("package:${context.packageName}")
            ContextCompat.startActivity(context, intent, null)
            true
        } catch (e: Exception) {
            false
        }


    }

    fun hasOverlayPermission(context: Context?): Boolean {
        var result = true
        try {
            val clazz: Class<*> = Settings::class.java
            val canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
            result = canDrawOverlays.invoke(null, context) as Boolean
        } catch (e: Exception) {
            Log.e("PermissionUtils", Log.getStackTraceString(e))
        }
        return result
    }
}