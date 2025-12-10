package com.zero.study.ui.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.toolkit.admob.manager.AdMobManager.dealConsentActionThen
import com.toolkit.admob.manager.AdMobManager.initMobileAds
import com.toolkit.admob.manager.OpenAdMobManager
import com.zero.base.activity.BaseActivity
import com.zero.base.util.StorageUtils
import com.zero.study.BuildConfig
import com.zero.study.databinding.ActivitySplashBinding
import com.zero.study.ui.model.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @date:2024/8/29 15:17
 * @path:com.zero.study.ui.activity.SplashActivity
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    private val viewModel: SplashViewModel by lazy {
        ViewModelProvider(this)[SplashViewModel::class.java]
    }
    private val timeFirst
        get() = StorageUtils.getBoolean(TIME_START, true)

    override fun initView() {
        if (!BuildConfig.DEBUG) {
            doNext(false)
            return
        }
        dealConsentActionThen(this@SplashActivity, { loadOpenAd() }, timeFirst)
    }

    override fun initData() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        initMobileAds(this, timeFirst)
        viewModel.getCountry().observe(this) { country ->
            Toast.makeText(this@SplashActivity, "IP:${country.ip}", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                delay(1000)
            }
        }
        viewModel.fetchCountry()
    }

    override fun addListener() {
    }

    private fun loadOpenAd() {
        val maxProgress = 700
        val maxDurationMs = 7_000L
        binding.loadingProgress.max = maxProgress
        binding.loadingProgress.progress = 0
        OpenAdMobManager.tryLoad()
        val animator = ObjectAnimator.ofInt(binding.loadingProgress, "progress", 0,
            maxProgress).apply {
            duration = maxDurationMs
            interpolator = DecelerateInterpolator(1.3f)
            doOnEnd {
                OpenAdMobManager.showAdIfAvailable(this@SplashActivity) {
                    doNext(timeFirst)
                }
            }
        }
        animator.start()
        lifecycleScope.launch {
            delay(2_000)
            while (OpenAdMobManager.isLoadingAd.get()) {
                delay(50)
            }
            if (animator.isRunning) animator.end()
        }
    }


    private fun doNext(isFirstTime: Boolean) {
        if (isFirstTime) {
            startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
        } else {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        }
        finish()
    }

    companion object {
        var TIME_START: String = "is_first_time_start"
    }
}