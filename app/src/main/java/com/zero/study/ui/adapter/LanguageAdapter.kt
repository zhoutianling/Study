package com.zero.study.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zero.study.databinding.ItemLanguageBinding
import com.zero.study.ui.adapter.LanguageAdapter.ItemViewHolder
import java.util.Locale

/**
 * @author Admin
 */
class LanguageAdapter(private var languageCodes: MutableList<String>) : RecyclerView.Adapter<ItemViewHolder>() {

    private var selected: Int = 0

    private fun selectIndex(index: Int) {
        val oldSelected = selected
        selected = index
        if (oldSelected >= 0) {
            notifyItemChanged(oldSelected)
        }
        if (selected >= 0) {
            notifyItemChanged(selected)
        }
    }

    fun initSelectedItem(code: String?) {
        for (index in languageCodes.indices) {
            if (code == languageCodes[index]) {
                selectIndex(index)
                return
            }
        }
    }

    fun getSelectedCode(): String {
        return languageCodes[selected]
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val code = languageCodes[position]
        val loc = Locale(code)
        holder.binding.tvLanguageName.text = loc.getDisplayLanguage(loc)
        holder.binding.tvLocalTitle.text = loc.displayLanguage
        holder.binding.ivSelect.visibility = if (selected == position) View.VISIBLE else View.INVISIBLE
        holder.binding.clLanguageRoot.isSelected = selected == position
        holder.itemView.setOnClickListener { selectIndex(position) }
    }

    override fun getItemCount(): Int {
        return languageCodes.size
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    class ItemViewHolder(var binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root)
}
