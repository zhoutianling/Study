package com.zero.health.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.zero.health.R
import java.io.BufferedReader
import java.io.FileReader
import kotlin.math.roundToInt

class MemoryMonitorOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var memoryInfoText: TextView? = null
    private var updateRunnable: Runnable? = null
    private val updateHandler = android.os.Handler(android.os.Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Settings.canDrawOverlays(this)) {
            createOverlayView()
            startUpdatingMemoryInfo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager?.let { wm ->
            overlayView?.let { view ->
                wm.removeView(view)
            }
        }
        updateRunnable?.let {
            updateHandler.removeCallbacks(it)
        }
    }

    private fun createOverlayView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_memory_monitor, null)

        memoryInfoText = overlayView?.findViewById(R.id.tvMemoryInfo)

        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT)
        } else {
            @Suppress("DEPRECATION") WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT)
        }

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100

        windowManager?.addView(overlayView, params)
    }

    private fun startUpdatingMemoryInfo() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateMemoryInfo()
                updateHandler.postDelayed(this, 2000) // 每2秒更新一次
            }
        }
        updateHandler.post(updateRunnable!!)
    }

    private fun updateMemoryInfo() {
        val (availableMemory, totalMemory, percentage) = getMemoryInfo()
        val availableMB = (availableMemory / (1024 * 1024)).toInt()
        val totalMB = (totalMemory / (1024 * 1024)).toInt()
        val memoryText = "可用内存: $availableMB MB / $totalMB MB ($percentage%)"
        memoryInfoText?.text = memoryText
    }

    private fun getMemoryInfo(): Triple<Long, Long, Int> {
        var totalMemory = 0L
        var availableMemory = 0L

        try {
            BufferedReader(FileReader("/proc/meminfo")).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.startsWith("MemTotal:")) {
                        totalMemory = line.split("\\s+".toRegex())[1].toLong() * 1024 // 转换为字节
                    } else if (line.startsWith("MemAvailable:")) {
                        availableMemory = line.split("\\s+".toRegex())[1].toLong() * 1024 // 转换为字节
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val percentage = if (totalMemory > 0) {
            ((availableMemory.toDouble() / totalMemory.toDouble()) * 100).roundToInt()
        } else {
            0
        }

        return Triple(availableMemory, totalMemory, percentage)
    }
}