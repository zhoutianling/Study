package com.zero.study.ui.adapter

import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.zero.study.R
import com.zero.study.databinding.UserItemLayoutBinding
import com.zero.study.db.entity.User

/**
 * @date:2024/10/28 16:05
 * @path:com.zero.study.ui.adapter.PagingAdapter
 */
class PagingAdapter : PagingDataAdapter<User, UserViewHolder>(ItemDiffCallback()) {
    var onDeleteClickListener: (user: User) -> Unit = {}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        user?.let {
            Glide.with(holder.itemView.context).load(user.avatar).transform(CircleCrop()).placeholder(R.mipmap.icon).error(R.mipmap.icon).into(holder.binding.avatar)
            holder.binding.name.text = "姓名：${user.aliasName}"
            holder.binding.ads.text = "地址：${user.address}"
            holder.binding.age.text = "年龄：${user.age}"
            holder.binding.itemDelete.setOnClickListener { onDeleteClickListener.invoke(user) }
        }
    }
}

class UserViewHolder(var binding: UserItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
class ItemDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}