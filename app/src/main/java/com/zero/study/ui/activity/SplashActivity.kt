package com.zero.study.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.toolkit.admob.listener.OpenAdStatusListener
import com.toolkit.admob.manager.AdMobManager.dealConsentActionThen
import com.toolkit.admob.manager.AdMobManager.initMobileAds
import com.toolkit.admob.manager.AppOpenAdManager
import com.zero.base.activity.BaseActivity
import com.zero.base.util.StorageUtils
import com.zero.study.AppStudy
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
        dealConsentActionThen(this@SplashActivity, { loadOpenAd(timeFirst) }, timeFirst)
    }

    override fun initData() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        initMobileAds(AppStudy.appContext, timeFirst)
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

    private fun loadOpenAd(timeFirst: Boolean) {
        AppOpenAdManager.tryWaitingAndLoadAd(this@SplashActivity, if (BuildConfig.DEBUG) 1 else 7, object : OpenAdStatusListener {
            override fun onNotReady(loadFailed: Boolean) {
            }

            override fun onComplete() {
                doNext(timeFirst)
            }
        })
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