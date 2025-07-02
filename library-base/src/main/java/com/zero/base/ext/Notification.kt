package com.zero.base.ext

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zero.library_base.R


@SuppressLint("ObsoleteSdkInt")
private fun Context.createNotificationChannelIfNeeded(channelId: String, name: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val serviceChannel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

}

@SuppressLint("MissingPermission")
fun Context.sendFullscreenNotification(configBlock: NotificationConfig.() -> Unit) {
    val config = NotificationConfig().apply(configBlock)

    createNotificationChannelIfNeeded(config.channelId, config.name)
    val builder = NotificationCompat.Builder(this, "Notification").setSmallIcon(config.iconRes).setContentTitle(config.title).setContentText(config.content).setPriority(NotificationCompat.PRIORITY_HIGH) // 设置高优先级
        .setAutoCancel(true)
    config.clickIntent?.let {
        val intent = PendingIntent.getActivity(this, System.currentTimeMillis().hashCode(), it, PendingIntent.FLAG_IMMUTABLE)
        builder.setFullScreenIntent(intent, true) // 设置全屏意图
    }


    val notificationManager = NotificationManagerCompat.from(this)
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        // TODO:  权限请求

        return
    }
    notificationManager.notify(config.notificationId, builder.build())
}


@SuppressLint("MissingPermission")
fun Context.notifyCustomTemplate(type: Int, configBlock: NotificationConfig.() -> Unit) {
    val config = NotificationConfig().apply(configBlock)

    createNotificationChannelIfNeeded(config.channelId, config.title)

    val pendingIntent = config.clickIntent?.let {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    val notification = NotificationCompat.Builder(this, config.channelId).setSmallIcon(config.iconRes).setContentTitle(config.title).setContentText(config.content).setContentIntent(pendingIntent).setAutoCancel(true).setOngoing(false).build()

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        // TODO: 权限请求
        return
    }

    NotificationManagerCompat.from(this).notify(type, notification)
}

@SuppressLint("MissingPermission")
fun Context.createCustomizeNotification(configBlock: NotificationConfig.() -> Unit) {
    val config = NotificationConfig().apply(configBlock)

    // 创建通知通道
    createNotificationChannelIfNeeded(config.channelId, config.title)
    // 点击意图
    val pendingIntent = config.clickIntent?.let {
        PendingIntent.getActivity(this, System.currentTimeMillis().hashCode(), it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    // 构建通知
    val notification = NotificationCompat.Builder(this, config.channelId).setSmallIcon(config.iconRes).setStyle(NotificationCompat.DecoratedCustomViewStyle()).setAutoCancel(true).apply {
            if (config.remoteViews != null) {
                setCustomContentView(config.remoteViews)
            }
            if (config.bigRemoteViews != null) {
                setCustomBigContentView(config.bigRemoteViews)
            }
            if (pendingIntent != null) {
                setContentIntent(pendingIntent)
            }
        }.build()

    NotificationManagerCompat.from(this).notify(config.notificationId, notification)
}

data class NotificationConfig(var title: String = "", var name: String = "", var content: String = "", var channelId: String = "default", var iconRes: Int = R.drawable.base_loading, var notificationId: Int = 1001, var bigRemoteViews: RemoteViews? = null, var remoteViews: RemoteViews? = null, var clickIntent: Intent? = null

)