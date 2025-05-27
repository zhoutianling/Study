package com.zero.study.receiver

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.zero.study.R
import java.util.*
import kotlin.concurrent.schedule

class ReplyMessageReceiver : BroadcastReceiver() {

    private val TAG = ReplyMessageReceiver::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        //获取回复消息的内容
        val inputContent = RemoteInput.getResultsFromIntent(intent)?.getCharSequence("key_text_reply")?.toString()
        Log.d(TAG, "onReceive: $inputContent")

        if (inputContent == null) {
            Log.e(TAG, "onReceive: 没有回复消息！")
            return
        }
        //构建回复消息通知
        val repliedNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, "replyChannelId")
        } else {
            NotificationCompat.Builder(context, "replyChannelId")
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setContentTitle("查看")//标题
            setContentText("已查看！")//内容
        }.build()

        val notificationManager = NotificationManagerCompat.from(context)
        //发送通知
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationManager.notify(2, repliedNotification)
        //1秒后取消通知
        Timer().schedule(1000) {
            notificationManager.cancel(2)
        }
    }
}