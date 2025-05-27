/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */
package com.zero.study.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zero.study.databinding.ItemHotwordBinding
import com.zero.study.bean.HotWord

/**
 * @author:zhoutl
 * @date:2024/6/6 21:24
 * @path:com.toolkit.openvpn.adapter.LineAdapter
 */
class HotWordAdapter : RecyclerView.Adapter<HotWordAdapter.HotSearchViewHolder>() {
    private val dataList: MutableList<HotWord> = ArrayList()

    fun add(data: HotWord) {
        dataList.add(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotSearchViewHolder {
        return HotSearchViewHolder(ItemHotwordBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: HotSearchViewHolder, position: Int) {
        val model = dataList[position]
        holder.binding.tvWord.text = model.name
        holder.binding.tvWord.setTextColor(model.id)
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<HotWord>() {
        override fun areItemsTheSame(oldItem: HotWord, newItem: HotWord): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: HotWord, newItem: HotWord): Boolean {
            return oldItem.name == newItem.name
        }

        override fun getChangePayload(oldItem: HotWord, newItem: HotWord): Any? {
            return super.getChangePayload(oldItem, newItem)
        }
    }

    class HotSearchViewHolder(var binding: ItemHotwordBinding) : RecyclerView.ViewHolder(binding.root)
}
