package com.toolkit.admob.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.toolkit.admob.AppLifecycle
import com.toolkit.admob.listener.AbstractLifecycleCallbacks
import com.toolkit.admob.manager.InterstitialAdManager.loadAd
import com.toolkit.admob_libray.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean


object AdMobManager {

    val atomicBoolean = AtomicBoolean(false)
    var appLifecycle: AppLifecycle? = null

    var connected = false
    fun initLifecycle(application: Application, filterClassName: String) {
        appLifecycle = AppLifecycle(application, filterClassName)
        initAdjust(application)
    }

    @JvmStatic
    fun initMobileAds(app: Context, timeFirst: Boolean) {
        if (atomicBoolean.getAndSet(true)) {
            showTips("-->Admob initialized ")
            return
        }
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            showTips("--> Admob initializing... ")
            MobileAds.initialize(app) {
                atomicBoolean.set(true)
                showTips("--> Admob complete... ")
                if (timeFirst) {
                    loadAd(app, BuildConfig.ADMOB_INTERSTITIAL_GUIDE)
                }
            }

        }
    }

    @JvmStatic
    fun dealConsentActionThen(activity: Activity, action: Runnable, timeFirst: Boolean) {
        val params: ConsentRequestParameters
        if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(activity).setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA).addTestDeviceHashedId("D36E7BC7027FB350A06CA34BBF35BFB7").build()
            params = ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()
        } else {
            params = ConsentRequestParameters.Builder().build()
        }
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(activity, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError: FormError? ->
                if (formError != null) {
                    Log.e("PVX", String.format(Locale.ENGLISH, "Error:-> %d: %s", formError.errorCode, formError.message))
                }
                if (consentInformation.canRequestAds()) {
                    initMobileAds(activity, timeFirst)
                }
                action.run()
            }
        }, { formError: FormError ->
            Log.e("PVX", String.format(Locale.ENGLISH, "Error: -> %d: %s", formError.errorCode, formError.message))
            if (consentInformation.canRequestAds()) {
                initMobileAds(activity, timeFirst)
            }
            action.run()
        })
        if (consentInformation.canRequestAds()) {
            initMobileAds(activity, timeFirst)
        }
    }

    fun initAdjust(application: Application) {
        val appToken = "3ak4l4s32xvk"
        val environment: String = if (BuildConfig.DEBUG) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(application, appToken, environment, true)
        config.setLogLevel(if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.SUPRESS)
        config.setAppSecret(1, 2120404030, 764732859, 2048063523, 614217887)
        config.isSendInBackground = true
        Adjust.onCreate(config)
        application.registerActivityLifecycleCallbacks(object : AbstractLifecycleCallbacks() {

            override fun onActivityResumed(activity: Activity) {
                Adjust.onResume()
            }

            override fun onActivityPaused(activity: Activity) {
                Adjust.onPause()
            }
        })
    }

    fun showTips(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.i("ad", "showTips: $msg")
        }
    }
}
