package com.zero.study.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zero.study.databinding.ItemBookLayoutBinding
import com.zero.study.ipc.Book

/**
 * @date:2024/10/17 20:17
 * @path:com.editor.plus.LogAdapter
 */
class BookAdapter : ListAdapter<Book, BookAdapter.BookViewHolder>(ItemDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(ItemBookLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val model = getItem(position)
        holder.binding.tvName.text = model.name
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.name == newItem.name
        }
    }

    inner class BookViewHolder(var binding: ItemBookLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}