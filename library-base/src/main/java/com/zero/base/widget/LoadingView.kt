package com.zero.base.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.zero.library_base.R

/**

 * @date:2024/5/28 15:48
 * @path:com.toolkit.base.widget.LoadingView
 */

@SuppressLint("ViewConstructor")
class LoadingView(context: Context, retryTask: Runnable?) : LinearLayoutCompat(context), View.OnClickListener {
    private val mTextView: TextView
    private val mRetryTask: Runnable?
    private val mImageView: ImageView

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.view_loading, this, true)
        mImageView = findViewById(R.id.image)
        mTextView = findViewById(R.id.text)
        this.mRetryTask = retryTask
    }

    fun setMsgViewVisibility(visible: Boolean) {
        mTextView.visibility = if (visible) VISIBLE else GONE
    }

    fun setStatus(status: Int) {
        var show = true
        var onClickListener: OnClickListener? = null
        var image = R.drawable.base_loading
        var str = 0
        when (status) {
            Gloading.STATUS_LOAD_SUCCESS -> show = false
            Gloading.STATUS_LOADING -> str = R.string.base_loading
            Gloading.STATUS_LOAD_FAILED -> {
                str = R.string.base_load_failed
                image = R.mipmap.ic_loading_failed
                onClickListener = this
            }

            Gloading.STATUS_EMPTY_DATA -> {
                str = R.string.base_load_empty
                image = R.mipmap.ic_loading_empty
            }

            else -> {}
        }
        mImageView.setImageResource(image)
        mImageView.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.baseColorWhite))
        setOnClickListener(onClickListener)
        mTextView.setText(str)
        visibility = if (show) VISIBLE else GONE
    }

    override fun onClick(v: View) {
        mRetryTask?.run()
    }
}
