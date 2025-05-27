package com.zero.study.ui.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zero.base.widget.RecyclerViewBanner

class RecyclerBanner @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerViewBanner<LinearLayoutManager?, RecyclerBanner.BannerAdapter?>(context, attrs, defStyleAttr) {
    override fun onBannerScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        if (bannerSize < 2) {
            return
        }
        val firstReal = mLayoutManager!!.findFirstVisibleItemPosition()
        val viewFirst = mLayoutManager!!.findViewByPosition(firstReal)
        val width = width.toFloat()
        if (width != 0f && viewFirst != null) {
            val right = viewFirst.right.toFloat()
            val ratio = right / width
            if (ratio > 0.8) {
                if (currentIndex != firstReal) {
                    currentIndex = firstReal
                    refreshIndicator()
                }
            } else if (ratio < 0.2) {
                if (currentIndex != firstReal + 1) {
                    currentIndex = firstReal + 1
                    refreshIndicator()
                }
            }
        }
    }

    override fun onBannerScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        val first = mLayoutManager?.findFirstVisibleItemPosition()
        val last = mLayoutManager?.findLastVisibleItemPosition()
        if (currentIndex != first && first == last && first != null) {
            currentIndex = first
            refreshIndicator()
        }
    }

    override fun getLayoutManager(context: Context, orientation: Int): LinearLayoutManager {
        return LinearLayoutManager(context, orientation, false)
    }

    override fun getAdapter(context: Context, list: List<String>?, onBannerItemClickListener: OnBannerItemClickListener?): BannerAdapter {
        return BannerAdapter(context, list, onBannerItemClickListener)
    }

    class BannerAdapter(private val context: Context, private val urlList: List<String>?, private val onBannerItemClickListener: OnBannerItemClickListener?) : RecyclerView.Adapter<BannerAdapter.BannerHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerHolder {
            return BannerHolder(ImageView(context))
        }

        override fun onBindViewHolder(holder: BannerHolder, @SuppressLint("RecyclerView") position: Int) {
            if (urlList.isNullOrEmpty()) {
                return
            }
            val url = urlList[position % urlList.size]
            val img = holder.itemView as ImageView
            Glide.with(context).load(url).into(img)
            img.setOnClickListener { onBannerItemClickListener?.onItemClick(position % urlList.size) }
        }

        override fun getItemCount(): Int {
            return Int.MAX_VALUE
        }

        inner class BannerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private var bannerItem: ImageView = itemView as ImageView

            init {
                val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                bannerItem.layoutParams = params
                bannerItem.scaleType = ImageView.ScaleType.FIT_XY
            }
        }
    }
}