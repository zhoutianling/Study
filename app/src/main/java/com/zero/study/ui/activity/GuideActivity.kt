package com.zero.study.ui.activity

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.toolkit.admob.manager.NativeAdManager
import com.toolkit.admob_libray.BuildConfig
import com.zero.study.R

import com.zero.base.activity.BaseActivity
import com.zero.study.databinding.ActivityGuideBinding

/**
 * @author Admin
 */
class GuideActivity : BaseActivity<ActivityGuideBinding>(ActivityGuideBinding::inflate) {
    private val guideItems = intArrayOf(R.layout.item_guide, R.layout.item_guide, R.layout.item_guide)
    private var mViews: MutableList<View>? = null
    private val mDotViews: MutableList<ImageView> = ArrayList()
    private var nativeAdManager: NativeAdManager? = null
    private fun setDotViews(position: Int) {
        for (index in mDotViews.indices) {
            mDotViews[index].setImageResource(if (index == position) R.drawable.shape_indicator_select else R.drawable.shape_indicator_unselect)
        }
    }

    private var mPagerAdapter: PagerAdapter = object : PagerAdapter() {
        override fun getCount(): Int {
            return guideItems.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(mViews!![position])
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val child = mViews!![position]
            container.addView(child)
            return child
        }
    }

    override fun initView() {
        nativeAdManager = NativeAdManager(this@GuideActivity, BuildConfig.NATIVE_BANNER_LANGUAGE, binding.adLayout, com.toolkit.admob_libray.R.layout.native_ad_admob_medium)
        for (item in guideItems) {
            mViews!!.add(layoutInflater.inflate(item, null))
            val dot = ImageView(this)
            dot.setImageResource(R.drawable.shape_indicator_select)
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.leftMargin = 20
            dot.layoutParams = layoutParams
            dot.isEnabled = false
            mDotViews.add(dot)
            binding.dotLayout.addView(dot)
        }
        binding.viewPager.adapter = mPagerAdapter
        setDotViews(0)
        binding.tvStart.setOnClickListener {
            startActivity(Intent(this@GuideActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun initData() {
        mViews = ArrayList()
    }

    override fun onResume() {
        super.onResume()
        nativeAdManager?.onUserVisible()
    }

    override fun onPause() {
        nativeAdManager?.onUserInvisible()
        super.onPause()
    }

    override fun onDestroy() {
        nativeAdManager?.onDestroy()
        super.onDestroy()
    }

    override fun addListener() {
        mViews?.get(0)?.findViewById<TextView>(R.id.tv_start)?.setOnClickListener {
            binding.viewPager.currentItem = 1
        }
        binding.viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val rootView = mViews?.get(position) as View
                val imageView = rootView.findViewById<ImageView>(R.id.iv_guide)
                val tvTitle = rootView.findViewById<TextView>(R.id.tv_title)
                val tvDes = rootView.findViewById<TextView>(R.id.tv_des)
                when (position) {
                    0 -> {
                        imageView.setImageResource(R.mipmap.ic_launcher)
                        tvTitle.text = getString(R.string.guide_item_one)
                        tvDes.text = getString(R.string.guide_item_one)
                    }

                    1 -> {
                        imageView.setImageResource(R.mipmap.ic_launcher)
                        tvTitle.text = getString(R.string.guide_item_one)
                        tvDes.text = getString(R.string.guide_item_one)
                    }

                    2 -> {
                        imageView.setImageResource(R.mipmap.ic_launcher)
                        tvTitle.text = getString(R.string.guide_item_one)
                        tvDes.text = getString(R.string.guide_item_one)
                    }
                }
                setDotViews(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })

    }
}