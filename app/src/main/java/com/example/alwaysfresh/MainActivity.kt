package com.example.alwaysfresh

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.alwaysfresh.databinding.ActivityMainBinding
import com.example.alwaysfresh.databinding.ItemFoodCardBinding
import com.example.alwaysfresh.viewmodel.DisplayItem
import com.example.alwaysfresh.viewmodel.MainViewModel

/**
 * VIEW — Displays the inventory summary and item list.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val addItemLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val name = data.getStringExtra(AddItemActivity.EXTRA_ITEM_NAME)
                ?: return@registerForActivityResult
            val date = data.getStringExtra(AddItemActivity.EXTRA_ITEM_DATE)
                ?: return@registerForActivityResult

            // Pass raw strings to ViewModel — View never creates Model objects
            viewModel.addItem(name, date)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Let the ViewModel handle Bundle restoration
        viewModel.restoreFromBundle(savedInstanceState)

        // Observe ViewModel LiveData — the View only renders, never calculates
        viewModel.freshCount.observe(this) { count ->
            binding.tvFresh.text = count.toString()
        }
        viewModel.expiringSoonCount.observe(this) { count ->
            binding.tvExpiringSoon.text = count.toString()
        }
        viewModel.expiredCount.observe(this) { count ->
            binding.tvExpired.text = count.toString()
        }
        viewModel.displayItems.observe(this) { items ->
            rebuildItemList(items)
        }

        binding.btnAddItem.setOnClickListener {
            addItemLauncher.launch(AddItemActivity.newIntent(this))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveToBundle(outState)
    }

    // ── Pure UI rendering
    private fun rebuildItemList(items: List<DisplayItem>) {
        binding.itemListContainer.removeAllViews()
        for (item in items) {
            val cardBinding = ItemFoodCardBinding.inflate(layoutInflater, binding.itemListContainer, false)
            val color = ContextCompat.getColor(this, item.statusColorResId)

            cardBinding.tvItemName.text = item.name
            cardBinding.tvItemDate.text = "Expires: ${item.date}"
            cardBinding.tvItemStatus.text = item.statusLabel
            cardBinding.tvItemStatus.setTextColor(color)
            cardBinding.statusIndicator.setBackgroundColor(color)

            binding.itemListContainer.addView(cardBinding.root)
        }
    }
}
