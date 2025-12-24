package com.zero.study

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * @date:2025/12/24 16:40
 * @path:com.zero.study.UpdateHelper
 * https://raw.githubusercontent.com/zhoutianling/Study/main/update/update.json
 */
object AppUpdateHelper {
    private const val APK_NAME = "study.apk"
    private const val UPDATE_JSON_URL = "https://raw.githubusercontent.com/zhoutianling/Study/main/update/update.json"
    suspend fun checkUpdate(context: Context) = withContext(Dispatchers.IO) {
        try {

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION") packageInfo.versionCode.toLong()
            }

            val jsonStr: String = Get<String>(UPDATE_JSON_URL).await()
            val json = JSONObject(jsonStr)

            val latestVersionCode = json.getInt("latestVersionCode")
            val updateMessage = json.optString("updateMessage", "发现新版本")
            val downloadUrl = json.optString("downloadUrl")
            val forceUpdate = json.optBoolean("forceUpdate", false)

            withContext(Dispatchers.Main) {
                if (latestVersionCode > currentVersionCode) {
                    showUpdateDialog(context, updateMessage, downloadUrl, forceUpdate)
                } else {
                    Toast.makeText(context, "已经是最新版本", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "检查更新失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUpdateDialog(context: Context, message: String, url: String,
                                 forceUpdate: Boolean) {
        val builder = AlertDialog.Builder(context).setTitle("发现新版本").setMessage(
            message).setPositiveButton("立即更新") { _, _ ->
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                downloadAndInstall(context, url)
            }
        }

        if (!forceUpdate) {
            builder.setNegativeButton("稍后") { dialog, _ ->
                dialog.dismiss()
            }
        } else {
            builder.setCancelable(false)
        }

        builder.show()
    }

    suspend fun downloadAndInstall(context: Context, url: String) = withContext(Dispatchers.IO) {
        try {
            val file: File = Get<File>(url) {
                setDownloadFileName(APK_NAME)
                setDownloadDir(context.filesDir)
                setDownloadMd5Verify(false)
                addDownloadListener(object : ProgressListener() {
                    override fun onProgress(p: Progress) {
                        CoroutineScope(Dispatchers.Main).launch {
                            if (p.finish) {
                                Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }.await()

            installApk(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val apkUri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileProvider",
            apkFile).also { intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }

}