package com.example.alwaysfresh.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alwaysfresh.data.AppDatabase
import com.example.alwaysfresh.data.InventoryRepository

/**
 * Factory that wires the production Room-backed [InventoryRepository] into [MainViewModel].
 * Exists so tests can construct [MainViewModel] with a fake repository instead.
 */
class MainViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val dao = AppDatabase.getInstance(application).itemDao()
            val repository = InventoryRepository(dao)
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
