package com.zero.base.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntDef
import com.zero.base.ext.dp
import com.zero.base.ext.px
import com.zero.library_base.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircleProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val mProgressRectF = RectF()
    private val mBoundsRectF = RectF()
    private val mProgressTextRect = Rect()

    private val mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mProgressBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mProgressTextPaint: Paint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var mRadius = 0f
    private var mCenterX = 0f
    private var mCenterY = 0f

    private var mProgress = 1
    private var mHeardRate = 0
    private var mMax = DEFAULT_MAX

    //Only work well in the Line Style, represents the line count of the rings included
    private var mLineCount = 0

    //Only work well in the Line Style, Height of the line of the progress bar
    private var mLineWidth = 0f

    //Stroke width of the progress of the progress bar
    private var mProgressStrokeWidth = 0f

    //Text size of the progress of the progress bar
    private var mProgressTextSize = 0f

    //Start color of the progress of the progress bar
    private var mProgressStartColor = 0

    //End color of the progress of the progress bar
    private var mProgressEndColor = 0

    //Color of the progress value of the progress bar
    private var mProgressTextColor = 0

    //Background color of the progress of the progress bar
    private var mProgressBackgroundColor = 0

    //the rotate degree of the canvas, default is -90.
    private var mStartDegree = 0

    // whether draw the background only outside the progress area or not
    private var mDrawBackgroundOutsideProgress = false

    // Format the current progress value to the specified format
    private var mProgressFormatter: ProgressFormatter? = DefaultProgressFormatter()

    // The style of the progress color
    @Style
    private var mStyle = 0

    // The Shader of mProgressPaint
    @ShaderMode
    private var mShader = 0

    // The Stroke Cap of mProgressPaint and mProgressBackgroundPaint
    private var mCap: Cap? = null

    // The blur radius of mProgressPaint
    private var mBlurRadius = 0

    // The blur style of mProgressPaint
    private var mBlurStyle: Blur? = null

    private val bpmText = "BPM"
    private val bpmTextRect = Rect()
    private var bpmBaseY: Float = 0f
    private var heartBitmap: Bitmap? = null
    private var heartBitmapDrawLeft: Float = 0f
    private var heartBitmapDrawTop: Float = 0f
    private var heartBitmapHeight: Int = 30.dp
    private var heartBitmapWidth: Int = 30.dp
    private val margin = 10.dp
    private var heartCacheBitmap: Bitmap? = null

    init {
        initFromAttributes(context, attrs)
        initPaint()
    }

    /**
     * Basic data initialization
     */
    private fun initFromAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar)
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.heart)
        val scale = heartBitmapHeight.toFloat() / originalBitmap.height
        heartBitmapWidth = (originalBitmap.width * scale).toInt()
        heartBitmap = Bitmap.createScaledBitmap(originalBitmap, heartBitmapWidth, heartBitmapHeight, true)
        mLineCount = a.getInt(R.styleable.CircleProgressBar_line_count, DEFAULT_LINE_COUNT)
        mStyle = a.getInt(R.styleable.CircleProgressBar_progress_style, LINE)
        mShader = a.getInt(R.styleable.CircleProgressBar_progress_shader, LINEAR)
        mCap = if (a.hasValue(R.styleable.CircleProgressBar_progress_stroke_cap)) Cap.entries.toTypedArray()[a.getInt(R.styleable.CircleProgressBar_progress_stroke_cap, 0)] else Cap.BUTT

        mLineWidth = a.getDimensionPixelSize(R.styleable.CircleProgressBar_line_width, DEFAULT_LINE_WIDTH.px).toFloat()
        mProgressTextSize = a.getDimensionPixelSize(R.styleable.CircleProgressBar_progress_text_size, DEFAULT_PROGRESS_TEXT_SIZE.px).toFloat()
        mProgressStrokeWidth = a.getDimensionPixelSize(R.styleable.CircleProgressBar_progress_stroke_width, DEFAULT_PROGRESS_STROKE_WIDTH.px).toFloat()

        mProgressStartColor = a.getColor(R.styleable.CircleProgressBar_progress_start_color, Color.parseColor(COLOR_FFF2A670))
        mProgressEndColor = a.getColor(R.styleable.CircleProgressBar_progress_end_color, Color.parseColor(COLOR_FFF2A670))
        mProgressTextColor = a.getColor(R.styleable.CircleProgressBar_progress_text_color, Color.parseColor(COLOR_FFF2A670))
        mProgressBackgroundColor = a.getColor(R.styleable.CircleProgressBar_progress_background_color, Color.parseColor(COLOR_FFD3D3D5))

        mStartDegree = a.getInt(R.styleable.CircleProgressBar_progress_start_degree, DEFAULT_START_DEGREE)
        mDrawBackgroundOutsideProgress = a.getBoolean(R.styleable.CircleProgressBar_drawBackgroundOutsideProgress, false)

        mBlurRadius = a.getDimensionPixelSize(R.styleable.CircleProgressBar_progress_blur_radius, 0)
        val blurStyle = a.getInt(R.styleable.CircleProgressBar_progress_blur_style, 0)
        mBlurStyle = when (blurStyle) {
            1 -> Blur.SOLID
            2 -> Blur.OUTER
            3 -> Blur.INNER
            else -> Blur.NORMAL
        }
        a.recycle()
    }

    /**
     * Paint initialization
     */
    private fun initPaint() {
        mProgressTextPaint.textAlign = Paint.Align.CENTER
        mProgressTextPaint.textSize = mProgressTextSize
        mProgressTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        mProgressPaint.style = if (mStyle == SOLID) Paint.Style.FILL else Paint.Style.STROKE
        mProgressPaint.strokeWidth = mProgressStrokeWidth
        mProgressPaint.color = mProgressStartColor
        mProgressPaint.strokeCap = mCap
        updateMaskBlurFilter()

        mProgressBackgroundPaint.style = if (mStyle == SOLID) Paint.Style.FILL else Paint.Style.STROKE
        mProgressBackgroundPaint.strokeWidth = mProgressStrokeWidth
        mProgressBackgroundPaint.color = mProgressBackgroundColor
        mProgressBackgroundPaint.strokeCap = mCap
    }


    private fun updateMaskBlurFilter() {
        if (mBlurStyle != null && mBlurRadius > 0) {
            setLayerType(LAYER_TYPE_SOFTWARE, mProgressPaint)
            mProgressPaint.setMaskFilter(BlurMaskFilter(mBlurRadius.toFloat(), mBlurStyle))
        } else {
            mProgressPaint.setMaskFilter(null)
        }
    }

    /**
     * The progress bar color gradient,
     * need to be invoked in the [.onSizeChanged]
     */
    private fun updateProgressShader() {
        if (mProgressStartColor != mProgressEndColor) {
            var shader: Shader? = null
            when (mShader) {
                LINEAR -> {
                    shader = LinearGradient(mProgressRectF.left, mProgressRectF.top, mProgressRectF.left, mProgressRectF.bottom, mProgressStartColor, mProgressEndColor, Shader.TileMode.CLAMP)
                    val matrix = Matrix()
                    matrix.setRotate(LINEAR_START_DEGREE, mCenterX, mCenterY)
                    shader.setLocalMatrix(matrix)
                }

                RADIAL -> {
                    shader = RadialGradient(mCenterX, mCenterY, mRadius, mProgressStartColor, mProgressEndColor, Shader.TileMode.CLAMP)
                }

                SWEEP -> {
                    //arc = radian * radius
                    val radian = (mProgressStrokeWidth / Math.PI * 2.0f / mRadius).toFloat()
                    val rotateDegrees = (-(if (mCap == Cap.BUTT && mStyle == SOLID_LINE) 0.0 else Math.toDegrees(radian.toDouble()))).toFloat()

                    shader = SweepGradient(mCenterX, mCenterY, intArrayOf(mProgressStartColor, mProgressEndColor), floatArrayOf(0.0f, 1.0f))
                    val matrix = Matrix()
                    matrix.setRotate(rotateDegrees, mCenterX, mCenterY)
                    shader.setLocalMatrix(matrix)
                }

                else -> {}
            }
            mProgressPaint.setShader(shader)
        } else {
            mProgressPaint.setShader(null)
            mProgressPaint.color = mProgressStartColor
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.rotate(mStartDegree.toFloat(), mCenterX, mCenterY)
        drawProgress(canvas)
        canvas.restore()
        drawProgressText(canvas)
    }

    private fun drawProgressText(canvas: Canvas) {
        if (mProgressFormatter == null) {
            return
        }
        val progressText = mHeardRate.toString()
        if (TextUtils.isEmpty(progressText)) {
            return
        }
        mProgressTextPaint.textSize = mProgressTextSize
        mProgressTextPaint.color = mProgressTextColor
        mProgressTextPaint.getTextBounds(progressText, 0, progressText.length, mProgressTextRect)
        val textBaseY = mCenterY + mProgressTextRect.height() / 2

        // 只绘制缓存图片
        heartCacheBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        // 再绘制进度文本
        canvas.drawText(progressText, 0, progressText.length, mCenterX, textBaseY, mProgressTextPaint)

        // 绘制 BPM（只用 onSizeChanged 计算好的位置和边界）
        val oldTextSize = mProgressTextPaint.textSize
        val oldTextColor = mProgressTextPaint.color
        mProgressTextPaint.textSize = mProgressTextSize * 0.6f
        mProgressTextPaint.color = Color.parseColor("#8A8A91")
        canvas.drawText(bpmText, 0, bpmText.length, mCenterX, bpmBaseY, mProgressTextPaint)
        mProgressTextPaint.textSize = oldTextSize
        mProgressTextPaint.color = oldTextColor
    }


    private fun drawProgress(canvas: Canvas) {
        when (mStyle) {
            SOLID -> drawSolidProgress(canvas)
            SOLID_LINE -> drawSolidLineProgress(canvas)
            LINE -> drawLineProgress(canvas)
            else -> drawLineProgress(canvas)
        }
    }

    /**
     * In the center of the drawing area as a reference point , rotate the canvas
     */
    private fun drawLineProgress(canvas: Canvas) {
        val unitDegrees = (2.0f * Math.PI / mLineCount).toFloat()
        val outerCircleRadius = mRadius
        val interCircleRadius = mRadius - mLineWidth

        val progressLineCount = (mProgress.toFloat() / mMax.toFloat() * mLineCount).toInt()

        for (i in 0 until mLineCount) {
            val rotateDegrees = i * -unitDegrees

            val startX = mCenterX + cos(rotateDegrees.toDouble()).toFloat() * interCircleRadius
            val startY = mCenterY - sin(rotateDegrees.toDouble()).toFloat() * interCircleRadius

            val stopX = mCenterX + cos(rotateDegrees.toDouble()).toFloat() * outerCircleRadius
            val stopY = mCenterY - sin(rotateDegrees.toDouble()).toFloat() * outerCircleRadius

            if (mDrawBackgroundOutsideProgress) {
                if (i >= progressLineCount) {
                    canvas.drawLine(startX, startY, stopX, stopY, mProgressBackgroundPaint)
                }
            } else {
                canvas.drawLine(startX, startY, stopX, stopY, mProgressBackgroundPaint)
            }

            if (i < progressLineCount) {
                canvas.drawLine(startX, startY, stopX, stopY, mProgressPaint)
            }
        }
    }

    /**
     * Just draw arc
     */
    private fun drawSolidProgress(canvas: Canvas) {
        if (mDrawBackgroundOutsideProgress) {
            val startAngle = MAX_DEGREE * mProgress / mMax
            val sweepAngle = MAX_DEGREE - startAngle
            canvas.drawArc(mProgressRectF, startAngle, sweepAngle, true, mProgressBackgroundPaint)
        } else {
            canvas.drawArc(mProgressRectF, 0.0f, MAX_DEGREE, true, mProgressBackgroundPaint)
        }
        canvas.drawArc(mProgressRectF, 0.0f, MAX_DEGREE * mProgress / mMax, true, mProgressPaint)
    }

    /**
     * Just draw arc
     */
    private fun drawSolidLineProgress(canvas: Canvas) {
        if (mDrawBackgroundOutsideProgress) {
            val startAngle = MAX_DEGREE * mProgress / mMax
            val sweepAngle = MAX_DEGREE - startAngle
            canvas.drawArc(mProgressRectF, startAngle, sweepAngle, false, mProgressBackgroundPaint)
        } else {
            canvas.drawArc(mProgressRectF, 0.0f, MAX_DEGREE, false, mProgressBackgroundPaint)
        }
        canvas.drawArc(mProgressRectF, 0.0f, MAX_DEGREE * mProgress / mMax, false, mProgressPaint)
    }

    /**
     * When the size of CircleProgressBar changed, need to re-adjust the drawing area
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBoundsRectF.left = paddingLeft.toFloat()
        mBoundsRectF.top = paddingTop.toFloat()
        mBoundsRectF.right = (w - paddingRight).toFloat()
        mBoundsRectF.bottom = (h - paddingBottom).toFloat()

        mCenterX = mBoundsRectF.centerX()
        mCenterY = mBoundsRectF.centerY()

        mRadius = (min(mBoundsRectF.width().toDouble(), mBoundsRectF.height().toDouble()) / 2).toFloat()

        mProgressRectF.set(mBoundsRectF)

        updateProgressShader()

        mProgressRectF.inset(mProgressStrokeWidth / 2, mProgressStrokeWidth / 2)

        // 计算 BPM 位置
        mProgressTextPaint.textSize = mProgressTextSize
        mProgressTextPaint.getTextBounds(mHeardRate.toString(), 0, mHeardRate.toString().length, mProgressTextRect)
        val textBaseY = mCenterY + mProgressTextRect.height() / 2
        mProgressTextPaint.textSize = mProgressTextSize * 0.6f
        mProgressTextPaint.getTextBounds(bpmText, 0, bpmText.length, bpmTextRect)
        bpmBaseY = textBaseY + margin + bpmTextRect.height()

        val progressTextTop = textBaseY - mProgressTextRect.height()
        heartBitmapDrawLeft = mCenterX - heartBitmapWidth / 2f
        heartBitmapDrawTop = progressTextTop - margin - heartBitmapHeight

        // 生成缓存 Bitmap，只绘制一次图片
        if (heartBitmap != null && width > 0 && height > 0) {
            heartCacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val cacheCanvas = Canvas(heartCacheBitmap!!)
            cacheCanvas.drawBitmap(heartBitmap!!, heartBitmapDrawLeft, heartBitmapDrawTop, null)
        }
    }


    public override fun onSaveInstanceState(): Parcelable {
        // Force our ancestor class to save its state
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.progress = mProgress
        return savedState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)

        progress = ss.progress
    }


    fun setProgressFormatter(progressFormatter: ProgressFormatter?) {
        mProgressFormatter = progressFormatter
        invalidate()
    }

    fun setProgressStrokeWidth(progressStrokeWidth: Float) {
        mProgressStrokeWidth = progressStrokeWidth

        mProgressRectF.set(mBoundsRectF)

        updateProgressShader()

        mProgressRectF.inset(mProgressStrokeWidth / 2, mProgressStrokeWidth / 2)

        invalidate()
    }

    fun setProgressTextSize(progressTextSize: Float) {
        mProgressTextSize = progressTextSize
        invalidate()
    }

    fun setProgressStartColor(progressStartColor: Int) {
        mProgressStartColor = progressStartColor
        updateProgressShader()
        invalidate()
    }

    fun setProgressEndColor(progressEndColor: Int) {
        mProgressEndColor = progressEndColor
        updateProgressShader()
        invalidate()
    }

    fun setProgressTextColor(progressTextColor: Int) {
        mProgressTextColor = progressTextColor
        invalidate()
    }

    fun setProgressBackgroundColor(progressBackgroundColor: Int) {
        mProgressBackgroundColor = progressBackgroundColor
        mProgressBackgroundPaint.color = mProgressBackgroundColor
        invalidate()
    }

    fun setLineCount(lineCount: Int) {
        mLineCount = lineCount
        invalidate()
    }

    fun setLineWidth(lineWidth: Float) {
        mLineWidth = lineWidth
        invalidate()
    }

    fun setStyle(@Style style: Int) {
        mStyle = style
        mProgressPaint.style = if (mStyle == SOLID) Paint.Style.FILL else Paint.Style.STROKE
        mProgressBackgroundPaint.style = if (mStyle == SOLID) Paint.Style.FILL else Paint.Style.STROKE
        invalidate()
    }

    fun setBlurRadius(blurRadius: Int) {
        mBlurRadius = blurRadius
        updateMaskBlurFilter()
        invalidate()
    }

    fun setBlurStyle(blurStyle: Blur?) {
        mBlurStyle = blurStyle
        updateMaskBlurFilter()
        invalidate()
    }

    fun setShader(@ShaderMode shader: Int) {
        mShader = shader
        updateProgressShader()
        invalidate()
    }

    fun setCap(cap: Cap?) {
        mCap = cap
        mProgressPaint.strokeCap = cap
        mProgressBackgroundPaint.strokeCap = cap
        invalidate()
    }

    fun setStartDegree(startDegree: Int) {
        mStartDegree = startDegree
        invalidate()
    }

    fun setDrawBackgroundOutsideProgress(drawBackgroundOutsideProgress: Boolean) {
        mDrawBackgroundOutsideProgress = drawBackgroundOutsideProgress
        invalidate()
    }

    var progress: Int
        get() = mProgress
        set(progress) {
            mProgress = progress
            invalidate()
        }

    var heartRate: Int
        get() = mHeardRate
        set(heartRate) {
            mHeardRate = heartRate
            invalidate()
        }

    var max: Int
        get() = mMax
        set(max) {
            mMax = max
            invalidate()
        }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(LINE, SOLID, SOLID_LINE)
    private annotation class Style

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(LINEAR, RADIAL, SWEEP)
    private annotation class ShaderMode

    interface ProgressFormatter {
        fun format(progress: Int, max: Int): CharSequence
    }

    private class DefaultProgressFormatter : ProgressFormatter {
        @SuppressLint("DefaultLocale")
        override fun format(progress: Int, max: Int): CharSequence {
            return String.format(DEFAULT_PATTERN, (progress.toFloat() / max.toFloat() * 100).toInt())
        }

        companion object {
            private const val DEFAULT_PATTERN = "%d%%"
        }
    }

    private class SavedState : BaseSavedState {
        var progress: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            progress = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(progress)
        }

        companion object {
            @JvmField
            val CREATOR: Creator<SavedState> = object : Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        const val LINE: Int = 0
        const val SOLID: Int = 1
        const val SOLID_LINE: Int = 2

        const val LINEAR: Int = 0
        const val RADIAL: Int = 1
        const val SWEEP: Int = 2

        private const val DEFAULT_MAX = 100
        private const val MAX_DEGREE = 360.0f
        private const val LINEAR_START_DEGREE = 90.0f

        private const val DEFAULT_START_DEGREE = -90

        private const val DEFAULT_LINE_COUNT = 45

        private const val DEFAULT_LINE_WIDTH = 4.0f
        private const val DEFAULT_PROGRESS_TEXT_SIZE = 11.0f
        private const val DEFAULT_PROGRESS_STROKE_WIDTH = 1.0f

        private const val COLOR_FFF2A670 = "#fff2a670"
        private const val COLOR_FFD3D3D5 = "#ffe3e3e5"

    }
}
