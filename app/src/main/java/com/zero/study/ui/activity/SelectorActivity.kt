package com.zero.study.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zero.base.activity.BaseActivity
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
    override fun initView() {
        imageView = binding.ivHeader
        commentPanel = binding.bottomLayout
        toolbar = binding.toolbarLayout.clToolbar
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.layoutManager = GridLayoutManager(this@SelectorActivity, 4)
        val albumList = ArrayList<Album>()
        repeat(150) {
            albumList.add(Album(R.mipmap.icon))
        }
        mAdapter.submitList(albumList)
    }


    private fun showCommentPanel() {

        // 展开动画
        val animator = ObjectAnimator.ofFloat(commentPanel, "y", commentPanel.height.toFloat(), 0f)
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener {
            updateUI(commentPanel.y)
        }
        animator.start()
    }

    private fun hideCommentPanel() {
        // 收起动画
        val animator = ObjectAnimator.ofFloat(commentPanel, "y", 0f, commentPanel.height.toFloat())
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener {
            updateUI(commentPanel.y)
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                commentPanel.visibility = View.GONE
            }
        })
        animator.start()
    }

    private fun updateUI(panelY: Float) {
        val maxImageScale = 0.7f
        val initialPanelHeight = commentPanel.height.toFloat()

        // 计算缩放比例 (从1.0到maxImageScale)
        val progress = (initialPanelHeight - panelY) / initialPanelHeight
        val scale = 1.0f - (1.0f - maxImageScale) * progress

        // 应用缩放
        imageView.scaleX = scale
        imageView.scaleY = scale

        // 调整图片位置 (向上偏移)
        val translateY = (imageView.height * (1.0f - scale)) / 2
        imageView.translationY = -translateY

        // 控制导航栏显示/隐藏
        toolbar.alpha = 1.0f - progress
    }

    override fun initData() {

    }


    override fun addListener() {
        binding.toolbarLayout.tvNext.setOnClickListener {
            showCommentPanel()
        }
        binding.toolbarLayout.ivBack.setOnClickListener { hideCommentPanel() }
    }
}
