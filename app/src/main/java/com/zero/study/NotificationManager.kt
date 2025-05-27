package com.zero.study

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.zero.study.receiver.ReplyMessageReceiver
import com.zero.study.ui.activity.LanguageActivity
import com.zero.study.ui.activity.PermissionManagerActivity

/**
 * @date:2024/12/2 17:11
 * @path:com.zero.study.NotificationManager
 */
object NotificationManager {
    //渠道Id
    private const val CHANNEL_ID = "${BuildConfig.APPLICATION_ID}.channelId"

    //渠道名
    private const val CHANNEL_NAME = "${BuildConfig.APPLICATION_ID}.channelName"

    //通知
    private lateinit var notification: Notification

    //回复通知
    private lateinit var replyNotification: Notification

    //横幅通知
    private lateinit var bannerNotification: Notification

    //常驻通知
    private lateinit var permanentNotification: Notification

    //自定义通知
    private lateinit var customNotification: Notification

    //通知Id
    private const val NOTIFICATION_ID = 0x10011

    //回复通知Id
    private const val REPLY_NOTIFICATION_ID = 0x10012

    //横幅通知Id
    private const val BANNER_NOTIFICATION_ID = 0x10013

    //常驻通知Id
    private const val PERMANENT_NOTIFICATION_ID = 0x10014

    //自定义通知Id
    private const val CUSTOM_NOTIFICATION_ID = 0x10015

    fun showNotification(context: Context, title: String, content: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        // 为DetailsActivity 创建显式 Intent
        val intent = Intent(context, LanguageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", title).putExtra("content", content)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE)
        notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建通知渠道
            notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT))
            NotificationCompat.Builder(context, CHANNEL_ID)
        } else {
            NotificationCompat.Builder(context, CHANNEL_ID)
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))//大图标（显示在通知上）
            setContentTitle(title)//标题
            setContentText(content)//内容
            setContentIntent(pendingIntent)//设置内容意图
            setAutoCancel(true)//设置自动取消
            setStyle(NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)))//设置样式
        }.build()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }


    fun showBackgroundNotification(service: Service, title: String, content: String) {
        val notificationManager = NotificationManagerCompat.from(service)
        val notificationIntent = Intent(service, PermissionManagerActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) }
        val pendingIntent = PendingIntent.getActivity(service, 0, notificationIntent, FLAG_IMMUTABLE)
        //构建通知
        permanentNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel("permanentChannelId", "permanentChannelName", NotificationManager.IMPORTANCE_HIGH))
            NotificationCompat.Builder(service, "permanentChannelId")
        } else {
            NotificationCompat.Builder(service, "permanentChannelId")
        }.apply {
            setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
            setFullScreenIntent(pendingIntent, true)
            setSmallIcon(R.drawable.ic_level_5)//小图标（显示在状态栏）
            setContentTitle(title)//标题
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setContentText(content)//内容
            setWhen(System.currentTimeMillis())//通知显示时间
        }.build()
        permanentNotification.flags = Notification.FLAG_ONGOING_EVENT
        if (ActivityCompat.checkSelfPermission(service, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        service.startForeground(PERMANENT_NOTIFICATION_ID, permanentNotification)
    }

    /**
     * 回复通知
     */
    fun showReplyNotification(context: Context, title: String, content: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        //远程输入
        val remoteInput = RemoteInput.Builder("key_text_reply").setLabel("快速回复").build()
        //构建回复pendingIntent
        val replyIntent = Intent(context, ReplyMessageReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, replyIntent, FLAG_MUTABLE)
        //点击通知的发送按钮
        val action = NotificationCompat.Action.Builder(0, "查看", pendingIntent).addRemoteInput(remoteInput).build()
        //构建通知
        replyNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel("replyChannelId", "replyChannelName", NotificationManager.IMPORTANCE_HIGH))
            NotificationCompat.Builder(context, "replyChannelId")
        } else {
            NotificationCompat.Builder(context, "replyChannelId")
        }.apply {
            setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))//大图标（显示在通知上）
            setContentTitle(title)//标题
            setContentText(content)//内容
            addAction(action)
        }.build()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationManager.notify(REPLY_NOTIFICATION_ID, replyNotification)
    }

    /**
     * 横幅通知
     */
    fun showBannerNotification(context: Context, title: String, content: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        val notificationIntent = Intent(context, PermissionManagerActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) }
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, FLAG_IMMUTABLE)
        //构建通知
        bannerNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel("msgChannelId", "msgChannelName", NotificationManager.IMPORTANCE_HIGH))
            NotificationCompat.Builder(context, "msgChannelId")
        } else {
            NotificationCompat.Builder(context, "msgChannelId")
        }.apply {
            setFullScreenIntent(pendingIntent, true)
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))//大图标（显示在通知上）
            setContentTitle(title)//标题
            setContentText(content)//内容
            setWhen(System.currentTimeMillis())//通知显示时间
            setAutoCancel(true)//设置自动取消
        }.build()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationManager.notify(BANNER_NOTIFICATION_ID, bannerNotification)
    }

    @SuppressLint("RemoteViewLayout")
    fun showCustomNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.layout_custom_notification)
        val bigRemoteViews = RemoteViews(context.packageName, R.layout.layout_custom_notification_big)
        customNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel("customChannelId", "customChannelName", NotificationManager.IMPORTANCE_HIGH))
            NotificationCompat.Builder(context, "customChannelId")
        } else {
            NotificationCompat.Builder(context, "customChannelId")
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setCustomContentView(remoteViews)
            setCustomBigContentView(bigRemoteViews)
            setOnlyAlertOnce(true)
            setOngoing(true)
        }.build()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationManager.notify(CUSTOM_NOTIFICATION_ID, customNotification)
    }
}