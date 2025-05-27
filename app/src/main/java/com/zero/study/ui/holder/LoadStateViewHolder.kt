package com.zero.study.ui.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.zero.study.R
import com.zero.study.databinding.ViewLoadingMoreBinding

/**
 * @date:2024/10/28 15:41
 * @path:com.zero.study.ui.holder.LoadStateViewHolder
 */
class LoadStateViewHolder(parent: ViewGroup, retry: () -> Unit) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_loading_more, parent, false)) {
    private val binding = ViewLoadingMoreBinding.bind(itemView)

    init {
        //当点击重试按钮时, 调用 PagingDataAdapter 的 retry() 重新尝试加载
        binding.btnLoadingRetry.setOnClickListener {
            retry()
        }
    }

    fun bind(loadState: LoadState) {
        // 当加载失败时.
        if (loadState is LoadState.Error) {
            // 将没有更多封装成 NoMoreException;  此时显示没有更多 View
            if (loadState.error is NoMoreException) {
                hideNoMoreUi(false) //显示 没有更多 View
                hideErrUi(true)     //隐藏 失败 View
            } else {
                hideNoMoreUi(true)
                hideErrUi(false, loadState.error.message)   //显示失败 View时, 填充错误 msg
            }
        } else {
            hideNoMoreUi(true)
            hideErrUi(true)
        }

        //加载中..
        binding.pbLoadingBar.visibility = if (loadState is LoadState.Loading) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    /**
     * 隐藏没有更多View;
     */
    private fun hideNoMoreUi(hide: Boolean) {
        if (hide) {
            binding.tvLoadingHint.visibility = View.GONE
        } else {
            binding.tvLoadingHint.visibility = View.VISIBLE
        }
    }

    /**
     * 隐藏 加载失败View;
     */
    private fun hideErrUi(hide: Boolean, msg: String? = null) {
        if (hide) {
            binding.tvLoadingError.visibility = View.GONE
            binding.btnLoadingRetry.visibility = View.GONE
        } else {
            binding.tvLoadingError.text = msg
            binding.tvLoadingError.visibility = View.VISIBLE
            binding.btnLoadingRetry.visibility = View.VISIBLE
        }
    }

    class NoMoreException : RuntimeException()
}