package com.example.alwaysfresh.model

import com.example.alwaysfresh.data.ItemDao
import com.example.alwaysfresh.data.ItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * REPOSITORY — Single source of truth for inventory data.
 * Wraps the Room DAO and contains business logic (freshness classification).
 */
class InventoryRepository(private val dao: ItemDao) {

    val activeItems: Flow<List<ItemEntity>> = dao.getActiveItems()

    val deletedItems: Flow<List<ItemEntity>> = dao.getDeletedItems()

    suspend fun addItem(name: String, expirationDate: String) {
        dao.insert(ItemEntity(name = name, expirationDate = expirationDate))
    }

    suspend fun getItemById(id: Long): ItemEntity? {
        return dao.getItemById(id)
    }

    suspend fun updateItem(item: ItemEntity) {
        dao.update(item)
    }

    suspend fun softDeleteItem(id: Long) {
        dao.softDelete(id, todayString())
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    companion object {

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

            val diffDays = (expiration.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)

            return when {
                diffDays < 0 -> FreshStatus.EXPIRED
                diffDays <= 7 -> FreshStatus.EXPIRING_SOON
                else -> FreshStatus.FRESH
            }
        }

        private fun todayString(): String {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DAY_OF_MONTH)
            return "%04d-%02d-%02d".format(year, month, day)
        }
    }
}
