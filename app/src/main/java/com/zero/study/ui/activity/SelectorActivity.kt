package com.zero.study.ui.activity

import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.GridLayoutManager
import com.zero.base.activity.BaseActivity
import com.zero.study.R
import com.zero.study.bean.Album
import com.zero.study.databinding.ActivitySelectorBinding
import com.zero.study.ui.adapter.AlbumAdapter

class SelectorActivity : BaseActivity<ActivitySelectorBinding>(ActivitySelectorBinding::inflate) {
    private val mAdapter: AlbumAdapter by lazy {
        AlbumAdapter()
    }
    private lateinit var bottomView: ConstraintLayout
    private lateinit var workView: AppCompatImageView
    private lateinit var toolbar: View
    private var isSelectMode = false
    private val maxImageScale = 0.7f

    override fun initView() {
        workView = binding.ivHeader
        bottomView = binding.bottomLayout
        toolbar = binding.toolbarLayout.clToolbar

        // 初始化RecyclerView
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = GridLayoutManager(this@SelectorActivity, 4)
            setHasFixedSize(true)
        }

        // 初始化数据
        val albumList = ArrayList<Album>()
        repeat(150) {
            albumList.add(Album(R.mipmap.icon))
        }
        mAdapter.submitList(albumList)

        // 初始状态设置
        setupInitialState()
    }

    private fun setupInitialState() {
        // 设置初始状态
        bottomView.layoutParams.height = 0
        bottomView.requestLayout()
        toolbar.alpha = 1f

        // 延迟一帧执行进入动画，确保布局已完成
        workView.post {
            startEnterAnimation()
        }
    }

    private fun startEnterAnimation() {
        val imageHeight = workView.height.toFloat()
        val toolbarHeight = toolbar.height.toFloat()

        val scaleAnimator = ValueAnimator.ofFloat(1f, maxImageScale)
        scaleAnimator.duration = 300
        scaleAnimator.interpolator = AccelerateDecelerateInterpolator()
        scaleAnimator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            val progress = (1f - scale) / (1f - maxImageScale)
            workView.scaleX = scale
            workView.scaleY = scale
            val translateY = -(toolbarHeight + (imageHeight * (1f - scale)) / 2)
            workView.translationY = translateY * progress
            toolbar.translationY = translateY * progress
            toolbar.alpha = 1f - progress
        }

        val panelHeightAnimator = ValueAnimator.ofInt(0, calculateBottomViewHeight())
        panelHeightAnimator.duration = 200
        panelHeightAnimator.interpolator = AccelerateDecelerateInterpolator()
        panelHeightAnimator.addUpdateListener { animation ->
            val height = animation.animatedValue as Int
            bottomView.layoutParams.height = height
            bottomView.requestLayout()
        }
        scaleAnimator.start()
        scaleAnimator.doOnEnd {
            panelHeightAnimator.start()
        }
        panelHeightAnimator.doOnEnd {
            isSelectMode = true
        }
    }

    private fun showCommentPanel() {
        if (isSelectMode) return
        isSelectMode = true

        val targetHeight = calculateBottomViewHeight()
        val animator = ValueAnimator.ofInt(bottomView.height, targetHeight)
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val height = animation.animatedValue as Int
            bottomView.layoutParams.height = height
            bottomView.requestLayout()
        }
        animator.start()
    }

    private fun hideCommentPanel() {
        if (!isSelectMode) return
        isSelectMode = false

        val imageHeight = workView.height.toFloat()
        val toolbarHeight = toolbar.height.toFloat()

        // 图片还原动画
        val scaleAnimator = ValueAnimator.ofFloat(maxImageScale, 1f)
        scaleAnimator.duration = 300
        scaleAnimator.interpolator = AccelerateDecelerateInterpolator()
        scaleAnimator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            val progress = (1f - scale) / (1f - maxImageScale)

            // 图片缩放
            workView.scaleX = scale
            workView.scaleY = scale

            // 图片向下移动，带动导航栏
            val translateY = -(toolbarHeight + (imageHeight * (1f - scale)) / 2)
            workView.translationY = translateY * progress
            toolbar.translationY = translateY * progress

            // 导航栏淡入
            toolbar.alpha = 1f - progress
        }

        // 面板收起动画
        val panelAnimator = ValueAnimator.ofInt(bottomView.height, 0)
        panelAnimator.duration = 300
        panelAnimator.interpolator = AccelerateDecelerateInterpolator()
        panelAnimator.addUpdateListener { animation ->
            val height = animation.animatedValue as Int
            bottomView.layoutParams.height = height
            bottomView.requestLayout()
        }

        // 同时执行两个动画
        scaleAnimator.start()
        panelAnimator.start()
    }

    private fun calculateBottomViewHeight(): Int {

        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        Log.d("TAG", "calculateBottomViewHeight: ${binding.root.height}")
        Log.d("TAG", "calculateBottomViewHeight: $screenHeight")
        Log.d("TAG", "calculateBottomViewHeight: $stateBarHeight")
        Log.d("TAG", "calculateBottomViewHeight: $navigationBarHeight")
        val imageHeight = workView.height.toFloat()
        val toolbarHeight = toolbar.height.toFloat()

     return  (screenHeight -  (workView.height * maxImageScale)).toInt()
    }

    override fun initData() {
        // 初始化数据
    }

    override fun addListener() {
        binding.toolbarLayout.tvNext.setOnClickListener {
            startEnterAnimation()
        }
        binding.toolbarLayout.ivBack.setOnClickListener {
            finish()
        }
        binding.ivClose.setOnClickListener {
            hideCommentPanel()
        }
    }
}
