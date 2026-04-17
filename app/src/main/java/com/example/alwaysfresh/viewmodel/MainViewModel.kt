package com.example.alwaysfresh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.alwaysfresh.R
import com.example.alwaysfresh.data.AppDatabase
import com.example.alwaysfresh.data.ItemEntity
import com.example.alwaysfresh.model.FreshStatus
import com.example.alwaysfresh.model.InventoryRepository
import kotlinx.coroutines.launch

data class DisplayItem(
    val id: Long,
    val name: String,
    val date: String,
    val statusLabel: String,
    val statusColorResId: Int
)

/**
 * VIEWMODEL — Shared across MainActivity and all its hosted fragments.
 *
 * Observes Room Flows via asLiveData() so the UI updates automatically
 * whenever the database changes. Uses viewModelScope for write operations.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository

    init {
        val dao = AppDatabase.getInstance(application).itemDao()
        repository = InventoryRepository(dao)
    }

    // ── Active items (not deleted) ──────────────────────────────────────
    private val allItems: LiveData<List<ItemEntity>> = repository.activeItems.asLiveData()

    val displayItems: LiveData<List<DisplayItem>> = allItems.map { list ->
        list.map { item ->
            val status = InventoryRepository.classifyItem(item.expirationDate)
            DisplayItem(
                id = item.id,
                name = item.name,
                date = item.expirationDate,
                statusLabel = statusLabel(status),
                statusColorResId = statusColor(status)
            )
        }
    }

    val freshCount: LiveData<Int> = allItems.map { list ->
        list.count { InventoryRepository.classifyItem(it.expirationDate) == FreshStatus.FRESH }
    }
    val expiringSoonCount: LiveData<Int> = allItems.map { list ->
        list.count { InventoryRepository.classifyItem(it.expirationDate) == FreshStatus.EXPIRING_SOON }
    }
    val expiredCount: LiveData<Int> = allItems.map { list ->
        list.count { InventoryRepository.classifyItem(it.expirationDate) == FreshStatus.EXPIRED }
    }

    // ── Deleted items (for Shopping List) ───────────────────────────────
    val deletedItems: LiveData<List<ItemEntity>> = repository.deletedItems.asLiveData()

    val deletedItemCount: LiveData<Int> = deletedItems.map { it.size }

    // ── Aggregate stats (for Waste Analytics) ───────────────────────────
    val totalItemCount: LiveData<Int> = allItems.map { activeList ->
        // We combine active + deleted counts. Since deletedItems is a separate
        // LiveData, we calculate from what we have — the WasteAnalyticsFragment
        // observes both independently for real-time accuracy.
        activeList.size + (deletedItems.value?.size ?: 0)
    }

    // ── Actions ─────────────────────────────────────────────────────────
    fun addItem(name: String, date: String) {
        viewModelScope.launch {
            repository.addItem(name, date)
        }
    }

    fun softDeleteItem(id: Long) {
        viewModelScope.launch {
            repository.softDeleteItem(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    // ── Private helpers ─────────────────────────────────────────────────
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
}
