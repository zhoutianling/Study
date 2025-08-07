package com.zero.health.helper

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.zero.base.ext.createNotification
import com.zero.base.ext.formatHourMinute
import com.zero.health.R
import com.zero.health.bean.RemindType
import com.zero.health.ui.activity.HeartRateActivity
import java.util.Date

/**
 * @date:2025/8/7 11:10
 * @path:com.zero.health.helper.NotifyHelper
 */
object NotifyHelper {
    //自定义通知Id
    fun showMainNotification(context: Service) {
        val mainNotification = context.createNotification {
            isPermanent = true
            iconRes = RemindType.getTypeIcon(RemindType.HEALTH_MAIN)
            channelId = RemindType.getNotificationChannelId(RemindType.HEALTH_MAIN)
            channelName = context.getString(RemindType.getTypeName(RemindType.HEALTH_MAIN))
            remoteViews = RemoteViews(context.packageName, R.layout.notification_main_normal).apply {
                remoteViews?.setTextViewText(R.id.tv_blood_pressure_value, "No Record")
                val flag = if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
                val clickIntent = Intent(context, HeartRateActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                remoteViews?.setOnClickPendingIntent(R.id.tv_blood_pressure_value, PendingIntent.getActivity(context, Activity.RESULT_OK, clickIntent, flag))
            }
            bigRemoteViews = RemoteViews(context.packageName, R.layout.notification_main_big).apply {

            }
        }
        context.startForeground(RemindType.getNotificationId(RemindType.HEALTH_MAIN), mainNotification)
    }

    fun showRemindNotification(context: Context, @RemindType.Type type: Int) {
        val remindNotification = context.createNotification {
            iconRes = RemindType.getTypeIcon(type)
            channelId = RemindType.getNotificationChannelId(type)
            channelName = context.getString(RemindType.getTypeName(type))
            remoteViews = RemoteViews(context.packageName, R.layout.notification_remind_normal).apply {
                setImageViewResource(R.id.iv_remind_logo, iconRes)
                setTextViewText(R.id.tv_remind_title, Date().formatHourMinute)
                setTextViewText(R.id.tv_remind_content, channelName)
                setTextViewText(R.id.tv_record, context.getString(R.string.health_record))
            }
            bigRemoteViews = RemoteViews(context.packageName, R.layout.notification_remind_big).apply {
                setImageViewResource(R.id.iv_remind_logo, R.drawable.ic_alarm_clock)
                setTextViewText(R.id.tv_remind_title, Date().formatHourMinute)
                setTextViewText(R.id.tv_remind_content, channelName)
                setTextViewText(R.id.tv_record, context.getString(R.string.health_record))
            }
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        NotificationManagerCompat.from(context).notify(RemindType.getNotificationId(type), remindNotification)
    }
}