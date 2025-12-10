package com.zero.study.ui.activity

import androidx.activity.addCallback
import com.toolkit.admob.manager.InterstitialPreloadAdMobManager
import com.toolkit.admob.manager.OpenAdMobManager
import com.toolkit.admob_libray.BuildConfig
import com.zero.base.activity.BaseActivity
import com.zero.study.databinding.ActivityInterstitialBinding

/**
 * @date:2024/8/30 18:12
 * @path:com.zero.study.ui.activity.InterstitialActivity
 */
class InterstitialActivity :
    BaseActivity<ActivityInterstitialBinding>(ActivityInterstitialBinding::inflate) {
    override fun initView() {
        InterstitialPreloadAdMobManager.preLoadInterstitialAd(
            BuildConfig.ADMOB_INTERSTITIAL_CONNECT_RESULT)
    }

    override fun initData() {
    }

    override fun addListener() {
        binding.ivFinish.setOnClickListener {
            finish()
        }
        binding.btnLoad.setOnClickListener {
            OpenAdMobManager.tryLoad()
        }
        binding.btnShow.setOnClickListener {
            OpenAdMobManager.showAdIfAvailable(this@InterstitialActivity) {

            }
        }
        onBackPressedDispatcher.addCallback(this) {
            InterstitialPreloadAdMobManager.tryShow(this@InterstitialActivity,
                BuildConfig.ADMOB_INTERSTITIAL_CONNECT_RESULT) {
                finish()
            }
        }
    }
}