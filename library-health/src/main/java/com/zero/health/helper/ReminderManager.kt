package com.zero.health.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zero.health.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val type = inputData.getInt("type", 0)
        val time = inputData.getString("time") ?: ""
        val repeat = inputData.getString("repeat") ?: "1111111"
        val label = inputData.getString("label") ?: ""

        val todayIndex = todayIndex()
        if (repeat.length == 7 && repeat[todayIndex] != '1') {
            return Result.success()
        }

        sendNotification(id = type + 100, title = "提醒", content = "$label ($time)")

        return Result.success()
    }

    private fun sendNotification(id: Int, title: String, content: String) {
        val channelId = "reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Scheduled Reminder",
                NotificationManager.IMPORTANCE_HIGH)
            val manager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId).setSmallIcon(
            R.drawable.ic_alarm_clock).setContentTitle(title).setContentText(content).setAutoCancel(
            true).setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }

    private fun todayIndex(): Int {
        val cal = Calendar.getInstance()
        val w = cal.get(Calendar.DAY_OF_WEEK)
        return if (w == Calendar.SUNDAY) 6 else w - 2
    }

    companion object {

        private fun computeDelayMs(timeStr: String): Long {
            val parts = timeStr.split(":")
            val targetHour = parts[0].toInt()
            val targetMinute = parts[1].toInt()

            val now = Calendar.getInstance()
            val target = Calendar.getInstance()

            target.set(Calendar.HOUR_OF_DAY, targetHour)
            target.set(Calendar.MINUTE, targetMinute)
            target.set(Calendar.SECOND, 0)
            target.set(Calendar.MILLISECOND, 0)

            if (target.timeInMillis <= now.timeInMillis) {
                target.add(Calendar.DAY_OF_MONTH, 1)
            }

            return target.timeInMillis - now.timeInMillis
        }

        fun schedule(context: Context, index: Int, type: Int, time: String, repeat: String,
                     label: String) {
            val delayMs = computeDelayMs(time)

            val data = Data.Builder().putInt("type", type).putString("time", time).putString(
                "repeat", repeat).putString("label", label).build()

            val tag = "reminder_tag"
            val onceName = "once_work_$index"
            val dailyName = "daily_work_$index"

            val onceReq = OneTimeWorkRequestBuilder<ReminderManager>().setInitialDelay(delayMs,
                TimeUnit.MILLISECONDS).addTag(tag).setInputData(data).build()

            WorkManager.getInstance(context).beginUniqueWork(onceName, ExistingWorkPolicy.REPLACE,
                onceReq).enqueue()

            val dailyReq = PeriodicWorkRequestBuilder<ReminderManager>(24, TimeUnit.HOURS, 15,
                TimeUnit.MINUTES).setInitialDelay(delayMs, TimeUnit.MILLISECONDS).addTag(
                tag).setInputData(data).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(dailyName,
                ExistingPeriodicWorkPolicy.KEEP, dailyReq)
        }

        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag("reminder_tag")
        }

        // 示例：设置十个不同的时间点提醒
        fun scheduleMultipleReminders(context: Context) {
            val reminders = mutableListOf<Triple<String, String, String>>()

            var totalMinutes = 0
            var count = 1

            while (totalMinutes < 24 * 60) {
                val hour = totalMinutes / 60
                val minute = totalMinutes % 60
                val timeString = "%02d:%02d".format(hour, minute)
                reminders.add(Triple(timeString, "1111111", "test$count"))

                totalMinutes += 20
                count++
            }

            for (i in reminders.indices) {
                val (time, repeat, label) = reminders[i]
                schedule(context = context, index = i, type = i, time = time, repeat = repeat,
                    label = label)
            }
        }
    }
}
