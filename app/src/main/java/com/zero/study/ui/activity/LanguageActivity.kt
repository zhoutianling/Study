package com.zero.study.ui.activity

import android.content.Intent
import android.view.KeyEvent
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.toolkit.admob.manager.InterstitialAdManager.loadAd
import com.toolkit.admob.manager.InterstitialAdManager.tryShow
import com.toolkit.admob.manager.NativeAdManager
import com.toolkit.admob_libray.BuildConfig
import com.toolkit.admob_libray.R
import com.zero.base.activity.BaseActivity
import com.zero.base.util.StorageUtils
import com.zero.base.util.StorageUtils.getBoolean
import com.zero.study.databinding.ActivityLanguageBinding
import com.zero.study.ui.adapter.LanguageAdapter
import java.util.Locale

/**
 * @author Admin
 */
class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {
    private var isFistTime = false
    private var nativeAdManager: NativeAdManager? = null
    private val stringArray = arrayOf("en", "es", "fr", "ar", "pt", "de", "ja", "ko", "ru", "hi", "in")
    private val selectedLanguageKey = "selectedCode"

    private val languageAdapter by lazy {
        LanguageAdapter(mutableListOf(*stringArray))
    }

    override fun initView() {
        nativeAdManager = NativeAdManager(this, BuildConfig.NATIVE_BANNER_LANGUAGE, binding.adLayout, R.layout.native_ad_admob_medium)
        loadAd(this, BuildConfig.ADMOB_INTERSTITIAL_LANGUAGE)
        isFistTime = getBoolean(SplashActivity.TIME_START, true)
        binding.ivBack.visibility = if (isFistTime) View.GONE else View.VISIBLE
        val initCode = StorageUtils.getString(selectedLanguageKey, "en")
        languageAdapter.initSelectedItem(initCode)
        binding.languageRecyclerview.adapter = languageAdapter
    }

    override fun initData() {
    }

    override fun addListener() {
        binding.ivBack.setOnClickListener { onKeyUp(KeyEvent.KEYCODE_BACK, KeyEvent(ACTION_UP, KeyEvent.KEYCODE_BACK)) }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFistTime) {
                    finish()
                } else {
                    tryShow(this@LanguageActivity, BuildConfig.ADMOB_INTERSTITIAL_LANGUAGE, { finish() }, true)
                }
            }
        })
        binding.tvNext.setOnClickListener {
            val selectedCode = languageAdapter.getSelectedCode()
            StorageUtils.putString(selectedLanguageKey, selectedCode)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(Locale(selectedCode)))
            if (isFistTime) {
                startActivity(Intent(this, MainActivity::class.java))
            }
            onKeyUp(KeyEvent.KEYCODE_BACK, KeyEvent(ACTION_UP, KeyEvent.KEYCODE_BACK))
        }
    }


    override fun onPause() {
        super.onPause()
        if (nativeAdManager != null) {
            nativeAdManager?.onUserInvisible()
        }
    }

    override fun onResume() {
        super.onResume()
        if (nativeAdManager != null) {
            nativeAdManager?.onUserVisible()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (nativeAdManager != null) {
            nativeAdManager?.onDestroy()
        }
    }
}