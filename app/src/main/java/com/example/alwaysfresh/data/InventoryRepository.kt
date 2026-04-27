package com.example.alwaysfresh.data

import com.example.alwaysfresh.model.FreshStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
        dao.softDelete(id, LocalDate.now().toString())
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    companion object {

        /**
         * Determines the freshness of an item based on its expiration date.
         *   EXPIRED: date is in the past, or [date] is unparseable
         *   EXPIRING_SOON: date is today or within the next 7 days
         *   FRESH: date is more than 7 days away
         *
         * [today] is overridable so unit tests can pin a deterministic "now."
         */
        fun classifyItem(date: String, today: LocalDate = LocalDate.now()): FreshStatus {
            val expiration = runCatching { LocalDate.parse(date) }.getOrNull()
                ?: return FreshStatus.EXPIRED
            val diffDays = ChronoUnit.DAYS.between(today, expiration)
            return when {
                diffDays < 0 -> FreshStatus.EXPIRED
                diffDays <= 7 -> FreshStatus.EXPIRING_SOON
                else -> FreshStatus.FRESH
            }
        }
    }
}
