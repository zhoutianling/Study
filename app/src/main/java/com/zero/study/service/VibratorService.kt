package com.zero.study.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * @date:2025/7/25 16:57
 * @path:com.zero.study.service.Vibrator
 */
class VibratorService(context: Context) : CoroutineScope, LifecycleObserver {
    // 协程相关
    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // 震动状态
    private var isVibrating = false
    private var vibrationJob: Job? = null

    // 开始周期性震动
    fun start(duration: Long = 100L, interval: Long = 1000L, strength: Int = 255) {
        if (isVibrating) return
        if (!vibrator.hasVibrator()) return
        Log.d("zzz", "start: job")
        isVibrating = true
        vibrationJob = launch {
            while (isActive) {
                vibrate(duration, strength)
                Log.d("zzz", "launch: job")
                delay(interval)
            }
        }
    }

    // 停止震动
    fun stop() {
        if (!isVibrating) return
        Log.d("zzz", "stop: job")
        isVibrating = false
        vibrationJob?.cancel()
        vibrationJob = null
        vibrator.cancel()
    }

    /**
     * 选择震动
     */
    fun select() {
        vibrate(18L, 235)
    }

    /**
     * 心率震动
     */
    fun heartRate() {
        vibrate(50L, 180)
    }

    private fun vibrate(mills: Long, strength: Int) {
        if (!vibrator.hasVibrator()) {
            return
        }
        val amplitude = if (strength in 1..255) strength else VibrationEffect.DEFAULT_AMPLITUDE
        val effect = VibrationEffect.createOneShot(mills, amplitude)
        vibrator.vibrate(effect)
    }

    fun destroy() {
        stop()
        job.cancel()
    }
}