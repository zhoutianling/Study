package com.zero.study.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zero.study.bean.Album
import com.zero.study.databinding.AlbumImageItemBinding

class AlbumAdapter() : ListAdapter<Album, AlbumAdapter.AlbumViewHolder>(ItemDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(AlbumImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val model = getItem(position)
        Glide.with(holder.binding.imageView).load(model.thumb).into(holder.binding.imageView)
    }


    class ItemDiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.thumb == newItem.thumb
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.thumb == newItem.thumb
        }
    }

    class AlbumViewHolder(var binding: AlbumImageItemBinding) : RecyclerView.ViewHolder(binding.root)
}