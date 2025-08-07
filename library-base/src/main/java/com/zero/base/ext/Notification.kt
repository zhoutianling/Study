package com.zero.base.ext

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zero.library_base.R


fun Context.createNotificationChannelIfNeeded(channelId: String, name: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val serviceChannel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}

/**
 * 自定义通知
 */
fun Context.createCustomizeNotification(configBlock: NotificationConfig.() -> Unit) {
    val config = NotificationConfig().apply(configBlock)

    // 创建通知通道
    createNotificationChannelIfNeeded(config.channelId, config.channelName)
    // 点击意图
    val pendingIntent = config.clickIntent?.let {
        PendingIntent.getActivity(this, System.currentTimeMillis().hashCode(), it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    // 构建通知
    val notification = NotificationCompat.Builder(this, config.channelId).setSmallIcon(config.iconRes).setOngoing(config.isPermanent).apply {
        if (config.isPermanent) {
            setPriority(NotificationCompat.PRIORITY_LOW)
        } else {
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
        }
        if (config.content.isNotBlank()) {
            setContentText(config.content)
        }
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
    config.notification = notification

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        return
    }
    NotificationManagerCompat.from(this).notify(config.notificationId, notification)
}

/**
 * 通过服务启动常驻通知 所以返回通知对象
 */
fun Context.createNotification(configBlock: NotificationConfig.() -> Unit): Notification {
    val config = NotificationConfig().apply(configBlock)
    createNotificationChannelIfNeeded(config.channelId, config.channelName)
    val pendingIntent = config.clickIntent?.let {
        PendingIntent.getActivity(this, System.currentTimeMillis().hashCode(), it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    // 构建通知
    val notification = NotificationCompat.Builder(this, config.channelId).setSmallIcon(config.iconRes).setOngoing(config.isPermanent).setContentTitle(config.title).apply {
        setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        if (config.isPermanent) {
            setPriority(NotificationCompat.PRIORITY_LOW)
        } else {
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
        }
        if (config.content.isNotBlank()) {
            setContentText(config.content)
        }
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
    config.notification = notification
    return notification

}


data class NotificationConfig(
    /**
     * 通知的标题
     */
    var title: String = "",
    /**
     * 常规的通知内容
     */
    var content: String = "",
    /**
     * 通知渠道id
     */
    var channelId: String = "default",
    /***
     * 通知渠道名称
     */
    var channelName: String = "default",
    /**
     * 通知图标
     */
    @DrawableRes var iconRes: Int = R.drawable.base_ic_circle,
    /**
     * 通知id
     */
    var notificationId: Int = 1001,
    /**
     * 自定义通知视图 大视图
     */
    var bigRemoteViews: RemoteViews? = null,
    /**
     * 自定义通知视图 小视图
     */
    var remoteViews: RemoteViews? = null,
    /**
     * 跳转对象
     */
    var clickIntent: Intent? = null,
    /**
     * 通知对象 用于更新通知
     */
    var notification: Notification? = null,
    /**
     * 是否是常驻通知
     */
    var isPermanent: Boolean = false

)