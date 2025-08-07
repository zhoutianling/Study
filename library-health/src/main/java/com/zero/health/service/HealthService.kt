package com.zero.health.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.zero.health.helper.NotifyHelper

class HealthService : Service() {

    override fun onCreate() {
        super.onCreate()
        NotifyHelper.showMainNotification(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}