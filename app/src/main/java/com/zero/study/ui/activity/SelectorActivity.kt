package com.zero.study.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zero.base.activity.BaseActivity
import com.zero.base.ext.dp
import com.zero.study.R
import com.zero.study.bean.Album
import com.zero.study.databinding.ActivitySelectorBinding
import com.zero.study.ui.adapter.AlbumAdapter

class SelectorActivity : BaseActivity<ActivitySelectorBinding>(ActivitySelectorBinding::inflate) {
    private val mAdapter: AlbumAdapter by lazy {
        AlbumAdapter()
    }
    private lateinit var commentPanel: ConstraintLayout
    private lateinit var imageView: AppCompatImageView
    private lateinit var toolbar: View
    private var isPanelExpanded = false
    private val maxImageScale = 0.3f

    override fun initView() {
        imageView = binding.ivHeader
        commentPanel = binding.bottomLayout
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
        commentPanel.layoutParams.height = 0
        commentPanel.requestLayout()
        toolbar.alpha = 1f

        // 延迟一帧执行进入动画，确保布局已完成
        imageView.post {
            startEnterAnimation()
        }
    }

    private fun startEnterAnimation() {
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        val imageHeight = imageView.height.toFloat()

        // 图片缩放动画
        val scaleAnimator = ValueAnimator.ofFloat(1f, maxImageScale)
        scaleAnimator.duration = 300
        scaleAnimator.interpolator = AccelerateDecelerateInterpolator()
        scaleAnimator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            imageView.scaleX = scale
            imageView.scaleY = scale
            imageView.translationY = -(imageHeight * (1f - scale)) / 2

            // 导航栏透明度跟随缩放进度
            toolbar.alpha = 1f - (1f - scale) / (1f - maxImageScale)
        }

        // 底部面板高度动画
        val panelHeightAnimator = ValueAnimator.ofInt(0, (screenHeight * 0.7f).toInt())
        panelHeightAnimator.duration = 300
        panelHeightAnimator.interpolator = AccelerateDecelerateInterpolator()
        panelHeightAnimator.addUpdateListener { animation ->
            val height = animation.animatedValue as Int
            commentPanel.layoutParams.height = height
            commentPanel.requestLayout()
        }

        // 先执行图片动画
        scaleAnimator.start()

        // 图片动画结束后执行面板动画
        scaleAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                panelHeightAnimator.start()
            }
        })

        // 面板动画结束后更新状态
        panelHeightAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isPanelExpanded = true
            }
        })
    }

    private fun showCommentPanel() {
        if (isPanelExpanded) return
        isPanelExpanded = true

        val targetHeight = (resources.displayMetrics.heightPixels * 0.7f).toInt()
        val animator = ValueAnimator.ofInt(commentPanel.height, targetHeight)
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val height = animation.animatedValue as Int
            commentPanel.layoutParams.height = height
            commentPanel.requestLayout()
        }
        animator.start()
    }

    private fun hideCommentPanel() {
        if (!isPanelExpanded) return
        isPanelExpanded = false

        val imageHeight = imageView.height.toFloat()

        // 图片还原动画
        val scaleAnimator = ValueAnimator.ofFloat(maxImageScale, 1f)
        scaleAnimator.duration = 300
        scaleAnimator.interpolator = AccelerateDecelerateInterpolator()
        scaleAnimator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            imageView.scaleX = scale
            imageView.scaleY = scale
            imageView.translationY = -(imageHeight * (1f - scale)) / 2

            // 导航栏透明度跟随缩放进度
            toolbar.alpha = 1f - (1f - scale) / (1f - maxImageScale)
        }

        // 面板收起动画
        val panelAnimator = ValueAnimator.ofInt(commentPanel.height, 0)
        panelAnimator.duration = 300
        panelAnimator.interpolator = AccelerateDecelerateInterpolator()
        panelAnimator.addUpdateListener { animation ->
            val height = animation.animatedValue as Int
            commentPanel.layoutParams.height = height
            commentPanel.requestLayout()
        }

        // 同时执行两个动画
        scaleAnimator.start()
        panelAnimator.start()

    }

    override fun initData() {
        // 初始化数据
    }

    override fun addListener() {
        binding.toolbarLayout.tvNext.setOnClickListener {
            showCommentPanel()
        }
        binding.toolbarLayout.ivBack.setOnClickListener {
            finish()
        }
        binding.ivClose.setOnClickListener {
            hideCommentPanel()
        }
    }
}
