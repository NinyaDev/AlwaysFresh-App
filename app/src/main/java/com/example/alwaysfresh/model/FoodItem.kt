package com.example.alwaysfresh.model

/**
 * MODEL — Represents a single food item in the fridge.
 */
data class FoodItem(
    val name: String,
    val expirationDate: String   // format: "YYYY-MM-DD"
)

/**
 * MODEL — The three possible freshness states for a food item.
 */
enum class FreshStatus {
    FRESH,
    EXPIRING_SOON,
    EXPIRED
}
