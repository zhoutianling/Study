package com.zero.health.bean

import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.zero.health.R

/**
 * @date:2025/8/7 11:48
 * @path:com.zero.health.bean.RemindType
 */

object RemindType {
    // 常量值
    const val HEALTH_MAIN = 0
    const val BLOOD_PRESSURE = 1
    const val HEART_RATE = 2
    const val BLOOD_GLUCOSE = 3

    private const val MAIN_NOTIFICATION_ID = 0x10011
    private const val REMIND_BLOOD_PRESSURE_NOTIFICATION_ID = 0x10012
    private const val REMIND_HEART_RATE_NOTIFICATION_ID = 0x10013
    private const val REMIND_BLOOD_GLUCOSE_NOTIFICATION_ID = 0x10014

    private const val CHANNEL_ID_MAIN = "com.oasis.health.reminder.main"
    private const val CHANNEL_ID_BLOOD_PRESSURE = "com.oasis.health.reminder.blood_pressure"
    private const val CHANNEL_ID_HEART_RATE = "com.oasis.health.reminder.heart_rate"
    private const val CHANNEL_ID_BLOOD_GLUCOSE = "com.oasis.health.reminder.blood_glucose"

    // 注解限制参数只能是以上常量
    @IntDef(HEALTH_MAIN, BLOOD_PRESSURE, HEART_RATE, BLOOD_GLUCOSE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type

    @StringRes
    fun getTypeName(@Type type: Int): Int {
        return when (type) {
            BLOOD_PRESSURE -> R.string.blood_pressure
            HEART_RATE -> R.string.heart_rate
            BLOOD_GLUCOSE -> R.string.blood_glucose
            HEALTH_MAIN -> R.string.health_main
            else -> throw IllegalArgumentException("Invalid reminder type")
        }
    }

    @DrawableRes
    fun getTypeIcon(@Type type: Int): Int {
        return when (type) {
            BLOOD_PRESSURE -> R.drawable.ic_blood_pressure
            HEART_RATE -> R.drawable.ic_heart_rate
            BLOOD_GLUCOSE -> R.drawable.ic_blood_glucose
            HEALTH_MAIN -> R.drawable.ic_alarm_clock
            else -> throw IllegalArgumentException("Invalid reminder type")
        }
    }

    fun getNotificationChannelId(@Type type: Int): String {
        return when (type) {
            BLOOD_PRESSURE -> CHANNEL_ID_BLOOD_PRESSURE
            HEART_RATE -> CHANNEL_ID_HEART_RATE
            BLOOD_GLUCOSE -> CHANNEL_ID_BLOOD_GLUCOSE
            HEALTH_MAIN -> CHANNEL_ID_MAIN
            else -> throw IllegalArgumentException("Invalid reminder type")
        }
    }

    fun getNotificationId(@Type type: Int): Int {
        return when (type) {
            BLOOD_PRESSURE -> REMIND_BLOOD_PRESSURE_NOTIFICATION_ID
            HEART_RATE -> REMIND_HEART_RATE_NOTIFICATION_ID
            BLOOD_GLUCOSE -> REMIND_BLOOD_GLUCOSE_NOTIFICATION_ID
            HEALTH_MAIN -> MAIN_NOTIFICATION_ID
            else -> throw IllegalArgumentException("Invalid reminder type")
        }
    }
}