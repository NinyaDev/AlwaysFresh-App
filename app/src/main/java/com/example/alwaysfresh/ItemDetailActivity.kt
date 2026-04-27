package com.example.alwaysfresh

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.alwaysfresh.data.AppDatabase
import com.example.alwaysfresh.data.InventoryRepository
import com.example.alwaysfresh.data.ItemEntity
import com.example.alwaysfresh.databinding.ActivityItemDetailBinding
import com.example.alwaysfresh.model.FreshStatus
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * VIEW — Full-screen detail view for a single inventory item.
 * Supports viewing, inline editing, and soft-deleting.
 */
class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var repository: InventoryRepository
    private var itemId: Long = -1
    private var currentItem: ItemEntity? = null
    private var isEditMode = false

    // In-flight edit form state captured by onSaveInstanceState. We hold it
    // until loadItem() finishes before applying, since the form widgets are
    // populated as part of the transition into edit mode.
    private var savedEditState: EditState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getInstance(application).itemDao()
        repository = InventoryRepository(dao)

        itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1)
        if (itemId == -1L) { finish(); return }

        savedEditState = savedInstanceState?.takeIf { it.getBoolean(KEY_EDIT_MODE, false) }
            ?.let { state ->
                EditState(
                    name = state.getString(KEY_EDIT_NAME) ?: "",
                    year = state.getInt(KEY_EDIT_YEAR, -1),
                    month = state.getInt(KEY_EDIT_MONTH, -1),
                    day = state.getInt(KEY_EDIT_DAY, -1)
                )
            }

        loadItem()
        setupButtons()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_EDIT_MODE, isEditMode)
        if (isEditMode) {
            outState.putString(KEY_EDIT_NAME, binding.etEditName.text.toString())
            outState.putInt(KEY_EDIT_YEAR, binding.editDatePicker.year)
            outState.putInt(KEY_EDIT_MONTH, binding.editDatePicker.month)
            outState.putInt(KEY_EDIT_DAY, binding.editDatePicker.dayOfMonth)
        }
    }

    private fun loadItem() {
        lifecycleScope.launch {
            currentItem = repository.getItemById(itemId)
            val item = currentItem ?: run { finish(); return@launch }
            displayItem(item)

            // If the user was editing pre-rotation, drop back into edit mode
            // and replay the in-flight form values.
            savedEditState?.let { state ->
                toggleEditMode(true)
                binding.etEditName.setText(state.name)
                if (state.year != -1) {
                    binding.editDatePicker.updateDate(state.year, state.month, state.day)
                }
                savedEditState = null
            }
        }
    }

    private fun displayItem(item: ItemEntity) {
        val status = InventoryRepository.classifyItem(item.expirationDate)
        val color = ContextCompat.getColor(this, statusColor(status))

        binding.tvName.text = item.name
        binding.tvExpirationDate.text = getString(R.string.expires_prefix) + " " + item.expirationDate
        binding.tvStatus.text = getString(R.string.status_prefix) + " " + statusLabel(status)
        binding.tvStatus.setTextColor(color)
        binding.tvDaysInfo.text = daysInfoText(item.expirationDate)
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnEdit.setOnClickListener { toggleEditMode(true) }
        binding.btnDelete.setOnClickListener { confirmDelete() }
        binding.btnSave.setOnClickListener { saveChanges() }
        binding.btnCancel.setOnClickListener { toggleEditMode(false) }
    }

    private fun toggleEditMode(editMode: Boolean) {
        isEditMode = editMode
        val item = currentItem ?: return

        // View mode views
        val viewVisibility = if (editMode) View.GONE else View.VISIBLE
        binding.tvName.visibility = viewVisibility
        binding.tvExpirationDate.visibility = viewVisibility
        binding.tvStatus.visibility = viewVisibility
        binding.tvDaysInfo.visibility = viewVisibility
        binding.btnEdit.visibility = viewVisibility
        binding.btnDelete.visibility = viewVisibility

        // Edit mode views
        val editVisibility = if (editMode) View.VISIBLE else View.GONE
        binding.etEditName.visibility = editVisibility
        binding.tvEditDateLabel.visibility = editVisibility
        binding.editDatePicker.visibility = editVisibility
        binding.btnSave.visibility = editVisibility
        binding.btnCancel.visibility = editVisibility

        if (editMode) {
            binding.etEditName.setText(item.name)
            val parsed = runCatching { LocalDate.parse(item.expirationDate) }.getOrNull()
                ?: LocalDate.now()
            binding.editDatePicker.updateDate(
                parsed.year,
                parsed.monthValue - 1,  // DatePicker is 0-indexed
                parsed.dayOfMonth
            )
        }
    }

    private fun saveChanges() {
        val name = binding.etEditName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etEditName.error = getString(R.string.errorEmptyName)
            return
        }

        val date = LocalDate.of(
            binding.editDatePicker.year,
            binding.editDatePicker.month + 1,
            binding.editDatePicker.dayOfMonth
        ).toString()

        val item = currentItem ?: return
        val updated = item.copy(name = name, expirationDate = date)

        lifecycleScope.launch {
            repository.updateItem(updated)
            currentItem = updated
            displayItem(updated)
            toggleEditMode(false)
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.delete_item) { _, _ ->
                lifecycleScope.launch {
                    repository.softDeleteItem(itemId)
                    finish()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun daysInfoText(date: String): String {
        val expiration = runCatching { LocalDate.parse(date) }.getOrNull()
            ?: return getString(R.string.expired_days, 0)
        val diffDays = ChronoUnit.DAYS.between(LocalDate.now(), expiration).toInt()

        return when {
            diffDays < 0 -> getString(R.string.expired_days, -diffDays)
            diffDays == 0 -> getString(R.string.expires_today)
            diffDays <= 7 -> getString(R.string.expiring_in_days, diffDays)
            else -> getString(R.string.days_fresh, diffDays)
        }
    }

    private fun statusLabel(status: FreshStatus): String = when (status) {
        FreshStatus.FRESH -> getString(R.string.freshLabel)
        FreshStatus.EXPIRING_SOON -> getString(R.string.expiringSoonLabel)
        FreshStatus.EXPIRED -> getString(R.string.expiredLabel)
    }

    private fun statusColor(status: FreshStatus): Int = when (status) {
        FreshStatus.FRESH -> R.color.fresh_green
        FreshStatus.EXPIRING_SOON -> R.color.warning_orange
        FreshStatus.EXPIRED -> R.color.expired_red
    }

    private data class EditState(
        val name: String,
        val year: Int,
        val month: Int,
        val day: Int
    )

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"

        private const val KEY_EDIT_MODE = "key_edit_mode"
        private const val KEY_EDIT_NAME = "key_edit_name"
        private const val KEY_EDIT_YEAR = "key_edit_year"
        private const val KEY_EDIT_MONTH = "key_edit_month"
        private const val KEY_EDIT_DAY = "key_edit_day"

        fun newIntent(context: Context, itemId: Long): Intent {
            return Intent(context, ItemDetailActivity::class.java).apply {
                putExtra(EXTRA_ITEM_ID, itemId)
            }
        }
    }
}
