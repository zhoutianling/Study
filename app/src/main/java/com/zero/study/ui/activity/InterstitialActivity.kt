package com.zero.study.ui.activity

import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import com.toolkit.admob.manager.InterstitialAdManager
import com.toolkit.admob_libray.BuildConfig
import com.zero.base.activity.BaseActivity
import com.zero.study.databinding.ActivityInterstitialBinding

/**
 * @date:2024/8/30 18:12
 * @path:com.zero.study.ui.activity.InterstitialActivity
 */
class InterstitialActivity : BaseActivity<ActivityInterstitialBinding>(ActivityInterstitialBinding::inflate) {
    override fun initView() {
        InterstitialAdManager.loadAd(this, BuildConfig.ADMOB_INTERSTITIAL_CONNECT_RESULT)
    }

    override fun initData() {
    }

    override fun addListener() {
        binding.ivFinish.setOnClickListener {
            onKeyUp(KeyEvent.KEYCODE_BACK, KeyEvent(ACTION_UP, KeyEvent.KEYCODE_BACK))
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                InterstitialAdManager.tryShow(this, BuildConfig.ADMOB_INTERSTITIAL_CONNECT_RESULT, { finish() }, true)
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}