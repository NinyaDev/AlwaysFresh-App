package com.example.alwaysfresh.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.alwaysfresh.R
import com.example.alwaysfresh.model.FoodItem
import com.example.alwaysfresh.model.FreshStatus
import com.example.alwaysfresh.model.InventoryRepository

data class DisplayItem(
    val name: String,
    val date: String,
    val statusLabel: String,
    val statusColorResId: Int
)

/**
 * VIEWMODEL — The connector between Model and View.
 *
 * It:
 *   1. Hold a reference to the Model (repository)
 *   2. Call Model methods when the View asks
 *   3. Expose results as LiveData so the View can observe them
 */
class MainViewModel : ViewModel() {

    // The Model
    private val repository = InventoryRepository()

    //  Observable state
    // Private mutable version — only the ViewModel can change it
    private val _items = MutableLiveData<List<FoodItem>>(emptyList())

    // Display-ready item list — the View observes this to build the ScrollView.
    val displayItems: LiveData<List<DisplayItem>> = _items.map { list ->
        list.map { item ->
            val status = repository.classifyItem(item.expirationDate)
            DisplayItem(
                name = item.name,
                date = item.expirationDate,
                statusLabel = statusLabel(status),
                statusColorResId = statusColor(status)
            )
        }
    }

    // Counter LiveData — recalculated automatically when _items changes.
    val freshCount: LiveData<Int> = _items.map {
        repository.countByStatus(FreshStatus.FRESH)
    }
    val expiringSoonCount: LiveData<Int> = _items.map {
        repository.countByStatus(FreshStatus.EXPIRING_SOON)
    }
    val expiredCount: LiveData<Int> = _items.map {
        repository.countByStatus(FreshStatus.EXPIRED)
    }

    // ── Actions (called by the View)
    // View passes raw strings — ViewModel tells the Model to create the item.
    fun addItem(name: String, date: String) {
        repository.addItem(name, date)
        _items.value = repository.getAllItems()
    }

    // Save inventory data into a Bundle (called by View in onSaveInstanceState). */
    fun saveToBundle(outState: Bundle) {
        val items = repository.getAllItems()
        outState.putStringArrayList(KEY_NAMES, ArrayList(items.map { it.name }))
        outState.putStringArrayList(KEY_DATES, ArrayList(items.map { it.expirationDate }))
    }

    // Restore inventory data from a Bundle (called by View in onCreate). */
    fun restoreFromBundle(savedInstanceState: Bundle?) {
        if (_items.value.isNullOrEmpty() && savedInstanceState != null) {
            val names = savedInstanceState.getStringArrayList(KEY_NAMES)
            val dates = savedInstanceState.getStringArrayList(KEY_DATES)
            if (names != null && dates != null) {
                val restored = names.zip(dates).map { (n, d) -> FoodItem(n, d) }
                repository.restoreItems(restored)
                _items.value = repository.getAllItems()
            }
        }
    }

    // ── Private helpers (translate Model types → View-friendly values) ─

    private fun statusLabel(status: FreshStatus): String = when (status) {
        FreshStatus.FRESH -> "Fresh"
        FreshStatus.EXPIRING_SOON -> "Expiring Soon"
        FreshStatus.EXPIRED -> "Expired"
    }

    private fun statusColor(status: FreshStatus): Int = when (status) {
        FreshStatus.FRESH -> R.color.fresh_green
        FreshStatus.EXPIRING_SOON -> R.color.warning_orange
        FreshStatus.EXPIRED -> R.color.expired_red
    }

    companion object {
        private const val KEY_NAMES = "key_item_names"
        private const val KEY_DATES = "key_item_dates"
    }
}
