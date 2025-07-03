package com.zero.study.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zero.study.R
import com.zero.study.databinding.ItemLineBinding
import com.zero.study.net.entity.Article
import com.zero.study.ui.adapter.LineAdapter.LineViewHolder

/**
 * @author:zhoutl
 * @date:2024/6/6 21:24
 * @path:com.toolkit.openvpn.adapter.LineAdapter
 */
class LineAdapter(val itemClickListener: (item: Article?) -> Unit) : ListAdapter<Article, LineViewHolder>(ItemDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        return LineViewHolder(ItemLineBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        holder.bindItem(getItem(position))
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains("ping")) {
            val model = getItem(position)
            holder.binding.tvDelayTime.text = model.delay
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    inner class LineViewHolder(var binding: ItemLineBinding) : RecyclerView.ViewHolder(binding.root) {
        private var article: Article? = null

        init {
            binding.root.setOnClickListener {
                itemClickListener.invoke(article)
            }
        }

        fun bindItem(item: Article) {
            this.article = item
            binding.ivConnectIcon.setImageResource(R.mipmap.ic_launcher)
            binding.tvIp.text = item.title.substring(7)
            binding.tvPraise.text = item.zan.toString()
            binding.tvDelayTime.text = item.delay
            binding.tvConnectState.isSelected = true
        }
    }
}
