package com.example.alwaysfresh.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alwaysfresh.R
import com.example.alwaysfresh.data.ItemEntity
import com.example.alwaysfresh.databinding.ItemFoodCardBinding

/**
 * RecyclerView Adapter for the Shopping List (soft-deleted items).
 * Reuses the same item_food_card layout but displays deletion info.
 */
class ShoppingListAdapter : ListAdapter<ItemEntity, ShoppingListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemFoodCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemEntity) {
            val context = binding.root.context

            binding.tvItemName.text = item.name
            binding.tvItemDate.text = context.getString(
                R.string.deleted_on,
                item.deletedDate ?: ""
            )
            binding.tvItemStatus.text = context.getString(R.string.shopping_list_tab)
            binding.tvItemStatus.setTextColor(
                context.getColor(R.color.warning_orange)
            )
            binding.statusIndicator.setBackgroundColor(
                context.getColor(R.color.warning_orange)
            )
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ItemEntity>() {
        override fun areItemsTheSame(oldItem: ItemEntity, newItem: ItemEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ItemEntity, newItem: ItemEntity): Boolean {
            return oldItem == newItem
        }
    }
}
