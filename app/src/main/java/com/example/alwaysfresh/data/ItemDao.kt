package com.example.alwaysfresh.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO — Defines all database operations for the items table.
 * Queries returning Flow are automatically observed; Room re-emits whenever data changes.
 */
@Dao
interface ItemDao {

    @Query("SELECT * FROM items WHERE isDeleted = 0 ORDER BY expirationDate ASC")
    fun getActiveItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE isDeleted = 1 ORDER BY deletedDate DESC")
    fun getDeletedItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): ItemEntity?

    @Insert
    suspend fun insert(item: ItemEntity)

    @Update
    suspend fun update(item: ItemEntity)

    @Query("UPDATE items SET isDeleted = 1, deletedDate = :deletedDate WHERE id = :id")
    suspend fun softDelete(id: Long, deletedDate: String)

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
