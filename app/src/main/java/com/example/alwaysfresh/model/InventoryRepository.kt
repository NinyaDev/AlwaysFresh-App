package com.example.alwaysfresh.model

import java.util.Calendar

/**
 * MODEL — Contains Logic
 *   - Store and retrieve food items
 *   - Classify items by freshness (Fresh / Expiring Soon / Expired)
 *   - Count items by status
 */
class InventoryRepository {

    private val items = mutableListOf<FoodItem>()

    // Add a new food item to the inventory.
    fun addItem(name: String, date: String) {
        items.add(FoodItem(name, date))
    }

    // Return a read-only copy of all items
    fun getAllItems(): List<FoodItem> = items.toList()

    // Replace the entire list (used when restoring from a Bundle).
    fun restoreItems(restoredItems: List<FoodItem>) {
        items.clear()
        items.addAll(restoredItems)
    }

    // Count how many items have a given freshness status.
    fun countByStatus(status: FreshStatus): Int {
        return items.count { classifyItem(it.expirationDate) == status }
    }

    /**
     * Determines the freshness of an item based on its expiration date:
     *   EXPIRED: date is in the past
     *   EXPIRING_SOON: date is today or within the next 7 days
     *   FRESH: date is more than 7 days away
     */
    fun classifyItem(date: String): FreshStatus {
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1   // Calendar months are 0-indexed
        val day = parts[2].toInt()

        val expiration = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        //Calculation done in Miliseconds
        val diffDays = (expiration.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)

        return when {
            diffDays < 0  -> FreshStatus.EXPIRED
            diffDays <= 7  -> FreshStatus.EXPIRING_SOON
            else           -> FreshStatus.FRESH
        }
    }
}
