package com.example.alwaysfresh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity — represents a single food item stored in the local database.
 */
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val expirationDate: String,        // format: "YYYY-MM-DD"
    val isDeleted: Boolean = false,
    val deletedDate: String? = null     // set when soft-deleted
)
