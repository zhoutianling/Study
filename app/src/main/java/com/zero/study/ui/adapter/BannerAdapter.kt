package com.zero.study.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zero.base.ext.dp
import com.zero.base.ext.loadImage
import com.zero.study.databinding.ItemBannerLayoutBinding
import com.zero.study.databinding.ItemBookLayoutBinding
import com.zero.study.ipc.Book

/**
 * @date:2024/10/17 20:17
 */
class BannerAdapter : ListAdapter<String, BannerAdapter.BannerViewHolder>(ItemDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        return BannerViewHolder(ItemBannerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val model = getItem(position)
        holder.binding.ivBanner.loadImage(model, 1)
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    inner class BannerViewHolder(var binding: ItemBannerLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}