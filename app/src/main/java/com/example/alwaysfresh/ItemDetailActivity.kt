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
import com.example.alwaysfresh.data.ItemEntity
import com.example.alwaysfresh.databinding.ActivityItemDetailBinding
import com.example.alwaysfresh.model.FreshStatus
import com.example.alwaysfresh.model.InventoryRepository
import kotlinx.coroutines.launch
import java.util.Calendar

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getInstance(application).itemDao()
        repository = InventoryRepository(dao)

        itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1)
        if (itemId == -1L) { finish(); return }

        loadItem()
        setupButtons()
    }

    private fun loadItem() {
        lifecycleScope.launch {
            currentItem = repository.getItemById(itemId)
            currentItem?.let { displayItem(it) } ?: finish()
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
            // Parse date and set DatePicker
            val parts = item.expirationDate.split("-")
            binding.editDatePicker.updateDate(
                parts[0].toInt(),
                parts[1].toInt() - 1,  // DatePicker is 0-indexed
                parts[2].toInt()
            )
        }
    }

    private fun saveChanges() {
        val name = binding.etEditName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etEditName.error = getString(R.string.errorEmptyName)
            return
        }

        val year = binding.editDatePicker.year
        val month = binding.editDatePicker.month + 1
        val day = binding.editDatePicker.dayOfMonth
        val date = "%04d-%02d-%02d".format(year, month, day)

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
        val parts = date.split("-")
        val expiration = Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffDays = ((expiration.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

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

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"

        fun newIntent(context: Context, itemId: Long): Intent {
            return Intent(context, ItemDetailActivity::class.java).apply {
                putExtra(EXTRA_ITEM_ID, itemId)
            }
        }
    }
}
