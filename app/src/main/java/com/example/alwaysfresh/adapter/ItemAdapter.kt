package com.example.alwaysfresh.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alwaysfresh.R
import com.example.alwaysfresh.databinding.ItemFoodCardBinding
import com.example.alwaysfresh.viewmodel.DisplayItem

/**
 * RecyclerView Adapter for displaying inventory items.
 * Uses ListAdapter + DiffUtil for efficient list updates from Room/LiveData.
 */
class ItemAdapter(
    private val onItemClick: (DisplayItem) -> Unit
) : ListAdapter<DisplayItem, ItemAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemFoodCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DisplayItem) {
            val color = ContextCompat.getColor(binding.root.context, item.statusColorResId)

            binding.tvItemName.text = item.name
            binding.tvItemDate.text = binding.root.context.getString(
                R.string.expires_prefix
            ) + " " + item.date
            binding.tvItemStatus.text = item.statusLabel
            binding.tvItemStatus.setTextColor(color)
            binding.statusIndicator.setBackgroundColor(color)

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DisplayItem>() {
        override fun areItemsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
            return oldItem == newItem
        }
    }
}
