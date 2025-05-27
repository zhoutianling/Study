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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.zero.study.R
import com.zero.study.databinding.UserItemLayoutBinding
import com.zero.study.db.entity.User

/**
 * @author:zhoutl
 * @date:2024/6/6 21:24
 * @path:com.toolkit.openvpn.adapter.LineAdapter
 */
class UserAdapter : ListAdapter<User, UserAdapter.UserViewHolder>(ItemDiffCallback()) {

    var onDeleteClickListener: (user: User) -> Unit = {}
    var onEditClickListener: (user: User) -> Unit = {}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position) ?: return
        with(user) {
            Glide.with(holder.itemView.context).load(user.avatar).transform(CircleCrop()).placeholder(R.mipmap.icon).error(R.mipmap.icon).into(holder.binding.avatar)
            holder.binding.name.text = "姓名：${aliasName}"
            holder.binding.ads.text = "地址：${address}"
            holder.binding.age.text = "年龄：${age}"
            holder.binding.itemDelete.setOnClickListener { onDeleteClickListener.invoke(this) }
            holder.binding.itemModify.setOnClickListener { onEditClickListener.invoke(this) }
        }
    }


    class ItemDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    class UserViewHolder(var binding: UserItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}
