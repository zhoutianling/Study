package com.zero.study.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.zero.study.R
import kotlin.math.abs

class PanelBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<ConstraintLayout>(context, attrs) {

    private var initialY: Float = 0f
    private var imageView: View? = null
    private var toolbar: View? = null
    private var maxImageScale = 0.3f
    private var minPanelHeight = 0f
    private var isExpanded = false
    private var lastY = 0f
    private var velocityTracker = 0f
    private var isAnimating = false

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ConstraintLayout, dependency: View): Boolean {
        if (dependency.id == R.id.iv_header) {
            imageView = dependency
        } else if (dependency.id == R.id.toolbar_layout) {
            toolbar = dependency
        }
        return dependency.id == R.id.iv_header || dependency.id == R.id.toolbar_layout
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: ConstraintLayout, layoutDirection: Int): Boolean {
        parent.onLayoutChild(child, layoutDirection)
        initialY = child.y
        // 计算图片底部位置作为面板最小高度
        imageView?.let { imgView ->
            minPanelHeight = imgView.y + imgView.height * maxImageScale
        }
        return true
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: ConstraintLayout, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL && !isAnimating
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: ConstraintLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (isAnimating) return

        if (dy > 0 && !isExpanded) {
            // 向上滑动，展开面板
            val newY = child.y - dy
            if (newY >= minPanelHeight) {
                child.y = newY
                consumed[1] = dy
                updateUI(child.y)
            }
        } else if (dy < 0 && isExpanded) {
            // 向下滑动，收起面板
            val newY = child.y - dy
            if (newY <= initialY) {
                child.y = newY
                consumed[1] = dy
                updateUI(child.y)
            }
        }
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: ConstraintLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        if (isAnimating) return

        if (dyUnconsumed < 0 && !isExpanded) {
            // 继续向上滑动
            val newY = child.y - dyUnconsumed
            if (newY >= minPanelHeight) {
                child.y = newY
                updateUI(child.y)
            }
        } else if (dyUnconsumed > 0 && isExpanded) {
            // 继续向下滑动
            val newY = child.y - dyUnconsumed
            if (newY <= initialY) {
                child.y = newY
                updateUI(child.y)
            }
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: ConstraintLayout, target: View, type: Int) {
        if (isAnimating) return

        val currentY = child.y
        val threshold = (initialY - minPanelHeight) / 2
        isAnimating = true

        val targetY = if (abs(currentY - initialY) < threshold) {
            isExpanded = false
            initialY
        } else {
            isExpanded = true
            minPanelHeight
        }

        child.animate()
            .y(targetY)
            .setDuration(300)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction {
                isAnimating = false
                updateUI(targetY)
            }
            .start()
    }

    private fun updateUI(panelY: Float) {
        imageView?.let { imgView ->
            val progress = (initialY - panelY) / (initialY - minPanelHeight)
            val scale = 1.0f - (1.0f - maxImageScale) * progress.coerceIn(0f, 1f)

            if (!scale.isNaN()) {
                imgView.scaleX = scale
                imgView.scaleY = scale

                val translateY = (imgView.height * (1.0f - scale)) / 2
                imgView.translationY = -translateY
            }
        }

        toolbar?.let { bar ->
            val alpha = 1.0f - (initialY - panelY) / initialY
            bar.alpha = alpha.coerceIn(0f, 1f)
        }
    }
}