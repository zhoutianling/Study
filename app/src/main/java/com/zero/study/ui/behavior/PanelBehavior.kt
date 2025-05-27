package com.zero.study.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.zero.study.R

class PanelBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<ConstraintLayout>(context, attrs) {

    private var initialY: Float = 0f
    private var imageView: View? = null
    private var toolbar: View? = null
    private var maxImageScale = 0.7f // 图片最大缩放比例
    private var minPanelHeight = 0f // 评论面板最小高度

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ConstraintLayout, dependency: View): Boolean {
        if (dependency.id == R.id.iv_header) {
            imageView = dependency
        } else if (dependency.id == R.id.toolbar_layout) {
            toolbar = dependency
        }
        return dependency.id == R.id.iv_header || dependency.id == R.id.toolbar_layout
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: ConstraintLayout, layoutDirection: Int): Boolean {
        // 记录评论面板的初始位置
        parent.onLayoutChild(child, layoutDirection)
        initialY = child.y
        minPanelHeight = child.height * 0.3f // 设置最小高度为原始高度的30%
        return true
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: ConstraintLayout, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: ConstraintLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        // 向上滑动时，先移动评论面板
        if (dy > 0) {
            val newY = child.y - dy
            if (newY >= minPanelHeight) {
                child.y = newY
                consumed[1] = dy
                updateUI(child.y)
            }
        }
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: ConstraintLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)

        // 向下滑动时，先移动评论面板
        if (dyUnconsumed < 0) {
            val newY = child.y - dyUnconsumed
            if (newY <= initialY) {
                child.y = newY
                updateUI(child.y)
            }
        }
    }

    private fun updateUI(panelY: Float) {
        imageView?.let { imgView ->
            // 计算缩放比例 (从1.0到maxImageScale)
            val progress = (initialY - panelY) / (initialY - minPanelHeight)
            val scale = 1.0f - (1.0f - maxImageScale) * progress

            // 应用缩放
            imgView.scaleX = scale
            imgView.scaleY = scale

            // 调整图片位置 (向上偏移)
            val translateY = (imgView.height * (1.0f - scale)) / 2
            imgView.translationY = -translateY
        }

        // 控制导航栏显示/隐藏
        toolbar?.let { bar ->
            val alpha = 1.0f - (initialY - panelY) / initialY
            bar.alpha = alpha
        }
    }
}