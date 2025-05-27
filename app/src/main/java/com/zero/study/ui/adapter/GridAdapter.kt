/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */
package com.zero.study.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zero.study.databinding.ItemImgBinding
import com.zero.study.net.entity.Article

/**
 * @author:zhoutl
 * @date:2024/6/6 21:24
 * @path:com.toolkit.openvpn.adapter.LineAdapter
 */
class GridAdapter : ListAdapter<Article, GridAdapter.LineViewHolder>(ItemDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        return LineViewHolder(ItemImgBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        val model = getItem(position)
    }


    class ItemDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    class LineViewHolder(var binding: ItemImgBinding) : RecyclerView.ViewHolder(binding.root)
}
