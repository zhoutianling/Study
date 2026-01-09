package com.zero.study.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowMetrics
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import coil.load
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.toolkit.admob_libray.BuildConfig
import com.zero.base.activity.BaseActivity
import com.zero.base.annotation.ViewBinding
import com.zero.base.ext.animateHeart
import com.zero.base.ext.dp
import com.zero.base.ext.onClick
import com.zero.base.ext.toast
import com.zero.study.R
import com.zero.study.databinding.ActivityWidgetBinding
import com.zero.study.ui.fragment.HomeFragment

/**
 * @author Admin
 */
class WidgetActivity : BaseActivity<ActivityWidgetBinding>(ActivityWidgetBinding::inflate),
    HomeFragment.OnClickListener {


    private val adSize: AdSize
        get() {
            val displayMetrics = resources.displayMetrics
            val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics: WindowMetrics = this.windowManager.currentWindowMetrics
                windowMetrics.bounds.width()
            } else {
                displayMetrics.widthPixels
            }
            val density = displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }


    private fun loadBanner() {
        val adView = AdView(this)
        adView.adUnitId = BuildConfig.NATIVE_BANNER_EDITOR
        adView.setAdSize(adSize)
        binding.layoutAd.removeView(adView)
        binding.layoutAd.addView(adView)
        val adRequest = AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java,
            bundleOf("collapsible" to "bottom")).build()
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                binding.skeletonLoading.visibility = View.GONE
            }
        }
        adView.loadAd(adRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewBinding.bind(this)
        loadBanner()
    }


    override fun initView() {
        val endAlphaAnim = endAlphaAnim()
        val startAlphaAnim = startAlphaAnim()
        binding.zoomView.setRange(-50f,50f,5f)
        binding.zoomView.show()


        binding.segmentSlide.setLabels(arrayOf("1","2","3","4","5"))

        binding.ivShadow.startAnimation(startAlphaAnim)
        binding.ivCompare.setImage(R.drawable.banner_1, R.drawable.banner_2)
        binding.ivCompare.setLineStyle(ContextCompat.getColor(this, R.color.colorPrimaryDark),
            1.0f.dp)
        binding.ivCompare.startAnimation(0f, 1f, 2000)

        binding.layoutRight.animateHeart()
        startAlphaAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                binding.ivShadow.startAnimation(endAlphaAnim)
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        endAlphaAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                binding.ivShadow.startAnimation(startAlphaAnim)
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
    }

    override fun initData() {
    }

    override fun addListener() {
        binding.btn03.onClick {
            Log.d("zzz", "setOnClickListener:${System.currentTimeMillis()} ")
            this.toast("500后可点击")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.ivCompare.stopAnimation()
    }


    companion object {
        const val DURATION: Int = 300

        private fun endAlphaAnim(): Animation {
            val animation: Animation = AlphaAnimation(0.5f, 1.0f)
            animation.interpolator = DecelerateInterpolator()
            animation.duration = DURATION.toLong()
            return animation
        }

        private fun startAlphaAnim(): Animation {
            val animation: Animation = AlphaAnimation(1.0f, 0.5f)
            animation.interpolator = DecelerateInterpolator()
            animation.duration = DURATION.toLong()
            return animation
        }
    }

    override fun onClickListener() {
        Toast.makeText(this@WidgetActivity, "click fragment", Toast.LENGTH_SHORT).show()
    }
}
