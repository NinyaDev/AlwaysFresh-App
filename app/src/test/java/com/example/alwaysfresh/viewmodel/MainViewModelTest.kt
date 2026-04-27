package com.example.alwaysfresh.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.alwaysfresh.data.InventoryRepository
import com.example.alwaysfresh.data.ItemDao
import com.example.alwaysfresh.data.ItemEntity
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeDao: FakeItemDao
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeItemDao()
        val app = mockk<Application>(relaxed = true)
        viewModel = MainViewModel(app, InventoryRepository(fakeDao))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `totalItemCount sums active and deleted`() = runTest {
        fakeDao.activeFlow.value = listOf(item(1), item(2))
        fakeDao.deletedFlow.value = listOf(item(3, isDeleted = true))

        viewModel.totalItemCount.observeForever {}
        advanceUntilIdle()

        assertEquals(3, viewModel.totalItemCount.value)
    }

    @Test
    fun `totalItemCount updates when only deleted items change`() = runTest {
        // Regression test: previous map-on-allItems implementation missed updates
        // that originated solely from the deleted-items flow.
        fakeDao.activeFlow.value = listOf(item(1))
        viewModel.totalItemCount.observeForever {}
        advanceUntilIdle()
        assertEquals(1, viewModel.totalItemCount.value)

        fakeDao.deletedFlow.value = listOf(item(2, isDeleted = true))
        advanceUntilIdle()
        assertEquals(2, viewModel.totalItemCount.value)
    }

    @Test
    fun `deletedItemCount tracks the deleted flow`() = runTest {
        fakeDao.deletedFlow.value = listOf(
            item(1, isDeleted = true),
            item(2, isDeleted = true)
        )

        viewModel.deletedItemCount.observeForever {}
        advanceUntilIdle()

        assertEquals(2, viewModel.deletedItemCount.value)
    }

    @Test
    fun `freshCount counts items dated more than seven days out`() = runTest {
        fakeDao.activeFlow.value = listOf(
            item(1, expirationDate = "2099-01-01"),
            item(2, expirationDate = "2099-06-15")
        )

        viewModel.freshCount.observeForever {}
        advanceUntilIdle()

        assertEquals(2, viewModel.freshCount.value)
    }

    @Test
    fun `expiredCount counts past-dated items`() = runTest {
        fakeDao.activeFlow.value = listOf(
            item(1, expirationDate = "2000-01-01"),
            item(2, expirationDate = "2099-01-01")
        )

        viewModel.expiredCount.observeForever {}
        advanceUntilIdle()

        assertEquals(1, viewModel.expiredCount.value)
    }

    private fun item(
        id: Long,
        expirationDate: String = "2099-12-31",
        isDeleted: Boolean = false
    ) = ItemEntity(
        id = id,
        name = "Item $id",
        expirationDate = expirationDate,
        isDeleted = isDeleted
    )
}

private class FakeItemDao : ItemDao {
    val activeFlow = MutableStateFlow<List<ItemEntity>>(emptyList())
    val deletedFlow = MutableStateFlow<List<ItemEntity>>(emptyList())

    override fun getActiveItems(): Flow<List<ItemEntity>> = activeFlow
    override fun getDeletedItems(): Flow<List<ItemEntity>> = deletedFlow
    override suspend fun getItemById(id: Long): ItemEntity? =
        activeFlow.value.find { it.id == id } ?: deletedFlow.value.find { it.id == id }

    override suspend fun insert(item: ItemEntity) {}
    override suspend fun update(item: ItemEntity) {}
    override suspend fun softDelete(id: Long, deletedDate: String) {}
    override suspend fun deleteAll() {}
}
