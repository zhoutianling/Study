package com.zero.base.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.zero.library_base.R
import kotlin.math.roundToInt

/**
 * 自定义带文字滑块的进度条
 * 支持动态设置区间 [minProgress, maxProgress]
 */
class TextThumbSlider @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    /* ---------------- 颜色配置 ---------------- */

    private val colorBg = ContextCompat.getColor(context, R.color.baseColorWhite)
    private val colorProgress = ContextCompat.getColor(context, R.color.baseColorAccent)
    private val colorTextLabel = ContextCompat.getColor(context, R.color.baseThemeColor)

    /* ---------------- 尺寸配置 ---------------- */

    private val thumbSize = 30.dp()
    private val trackHeight = 38.dp()
    private val gap = 2.dp()

    /* ---------------- 画笔 ---------------- */

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorBg
        style = Paint.Style.FILL
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorProgress
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /* ---------------- 状态属性 ---------------- */

    private var minProgress = 0
    private var maxProgress = 100
    private var _progress = 22f // 当前进度值

    /**
     * 当前进度数值
     */
    var progress: Int
        get() = _progress.roundToInt()
        set(value) {
            val newValue = value.coerceIn(minProgress, maxProgress).toFloat()
            if (_progress != newValue) {
                _progress = newValue
                onProgressChangeListener?.onProgressChanged(this, _progress.roundToInt(), false)
                invalidate()
            }
        }

    /* ---------------- 核心方法 ---------------- */

    /**
     * 设置进度区间及当前值
     */
    fun setRange(min: Int, max: Int, current: Int = min) {
        if (min >= max) {
            Log.e("TextThumbSlider", "min ($min) 必须小于 max ($max)")
            return
        }
        this.minProgress = min
        this.maxProgress = max
        this.progress = current
        invalidate()
    }

    /* ---------------- 回调 ---------------- */

    interface OnProgressChangeListener {
        fun onProgressChanged(slider: TextThumbSlider, progress: Int, fromUser: Boolean)
    }

    var onProgressChangeListener: OnProgressChangeListener? = null

    /* ---------------- 绘制逻辑 ---------------- */

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val centerY = h / 2f

        // 计算滑块可移动的起始和结束 X 坐标
        val startX = gap + thumbSize / 2f + gap
        val endX = w - gap - thumbSize / 2f - gap
        val usableWidth = endX - startX

        // 根据当前进度计算比例 (0.0 ~ 1.0)
        val range = (maxProgress - minProgress).coerceAtLeast(1).toFloat()
        val ratio = (_progress - minProgress) / range
        val thumbX = startX + ratio * usableWidth
        val thumbRightEdge = thumbX + thumbSize / 2f

        /* 1. 绘制轨道背景 */
        trackPaint.color = colorBg
        val trackRect = RectF(0f, centerY - trackHeight / 2f, w, centerY + trackHeight / 2f)
        canvas.drawRoundRect(trackRect, trackHeight / 2f, trackHeight / 2f, trackPaint)

        /* 2. 绘制进度条（带滑块位置镂空效果） */
        val progressRect = RectF(gap, centerY - trackHeight / 2f + gap, thumbRightEdge + gap,
            centerY + trackHeight / 2f - gap)

        val progressPath = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            val radius = (trackHeight - gap * 2) / 2f
            addRoundRect(progressRect, radius, radius, Path.Direction.CW)
            // 镂空滑块圆圈
            addCircle(thumbX, centerY, thumbSize / 2f, Path.Direction.CW)
        }
        canvas.drawPath(progressPath, progressPaint)

        /* 3. 绘制两侧 A- / A+ 标签 */
        textPaint.color = colorTextLabel
        textPaint.textSize = 14.sp()
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("A-", 14.dp(), centerY + baseline(textPaint), textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("A+", w - 14.dp(), centerY + baseline(textPaint), textPaint)

        /* 4. 绘制实心滑块 */
        trackPaint.color = colorBg // 滑块使用白色
        canvas.drawCircle(thumbX, centerY, thumbSize / 2f, trackPaint)

        /* 5. 绘制滑块内部数字 */
        textPaint.color = colorTextLabel
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(_progress.roundToInt().toString(), thumbX, centerY + baseline(textPaint),
            textPaint)
    }

    /* ---------------- 触摸事件 ---------------- */

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 这里的边界计算需与 onDraw 保持一致以获得精准手感
        val startX = gap + thumbSize / 2f + gap
        val endX = width - gap - thumbSize / 2f - gap
        val usableWidth = endX - startX

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                handleTouchUpdate(event.x, startX, endX, usableWidth)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                handleTouchUpdate(event.x, startX, endX, usableWidth)
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                performClick()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleTouchUpdate(x: Float, startX: Float, endX: Float, usableWidth: Float) {
        val clampedX = x.coerceIn(startX, endX)
        val ratio = (clampedX - startX) / usableWidth

        val range = (maxProgress - minProgress).toFloat()
        val calculatedValue = minProgress + (ratio * range)
        val finalInt = calculatedValue.roundToInt().coerceIn(minProgress, maxProgress)

        if (_progress.roundToInt() != finalInt) {
            _progress = finalInt.toFloat()
            onProgressChangeListener?.onProgressChanged(this, finalInt, true)
            invalidate()
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    /* ---------------- 状态持久化 ---------------- */

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("super_state", super.onSaveInstanceState())
            putFloat("current_progress", _progress)
            putInt("min_val", minProgress)
            putInt("max_val", maxProgress)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            minProgress = state.getInt("min_val")
            maxProgress = state.getInt("max_val")
            _progress = state.getFloat("current_progress")
            super.onRestoreInstanceState(state.getParcelable("super_state"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    /* ---------------- 辅助工具 ---------------- */

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = SeekBar::class.java.name
        info.contentDescription = "进度调节: ${progress}"
    }

    private fun baseline(paint: Paint): Float {
        val fm = paint.fontMetrics
        return (fm.descent - fm.ascent) / 2f - fm.descent
    }

    private fun Int.dp(): Float = this * resources.displayMetrics.density
    private fun Int.sp(): Float = this * resources.displayMetrics.scaledDensity
}