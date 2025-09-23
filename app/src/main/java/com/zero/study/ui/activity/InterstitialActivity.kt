package com.zero.study.ui.activity

import androidx.activity.OnBackPressedCallback
import com.toolkit.admob.manager.InterstitialAdManager
import com.toolkit.admob.manager.InterstitialAdManager.tryShow
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
            tryShow(this@InterstitialActivity, BuildConfig.ADMOB_INTERSTITIAL_CONNECT_RESULT, { finish() }, true)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                tryShow(this@InterstitialActivity, BuildConfig.ADMOB_INTERSTITIAL_CONNECT_RESULT, { finish() }, true)
            }
        })
    }
}