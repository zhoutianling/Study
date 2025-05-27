package com.zero.study

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import com.toolkit.admob.manager.AdMobManager.initLifecycle
import com.zero.base.net.RetrofitManager
import com.zero.base.util.StorageUtils
import com.zero.base.widget.Gloading
import com.zero.base.widget.LoadingAdapter
import com.zero.study.ui.activity.SplashActivity
import kotlin.properties.Delegates

class AppStudy : Application() {


    companion object {
        var appContext: Context by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        if (isMainProcess(this)) {
            initLifecycle(this, SplashActivity::class.java.getSimpleName())
            StorageUtils.init(this)
            Gloading.default?.initDefault(LoadingAdapter())
            RetrofitManager.initHttp(this)
        }
    }

    private fun isMainProcess(context: Context): Boolean {
        val myPid = Process.myPid()
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val processes = activityManager.runningAppProcesses
        if (processes != null) {
            for (processInfo in processes) {
                if (processInfo.pid == myPid) {
                    return context.packageName == processInfo.processName
                }
            }
        }
        return false
    }
}
