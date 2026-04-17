package com.example.alwaysfresh

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.alwaysfresh.databinding.ActivityAddItemBinding

/**
 * VIEW — Input form for adding a new food item.
 */
class AddItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restore state after rotation
        if (savedInstanceState != null) {
            binding.etItemName.setText(savedInstanceState.getString(KEY_NAME, ""))
            val year = savedInstanceState.getInt(KEY_YEAR)
            val month = savedInstanceState.getInt(KEY_MONTH)
            val day = savedInstanceState.getInt(KEY_DAY)
            binding.datePicker.updateDate(year, month, day)
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSaveItem.setOnClickListener {
            val name = binding.etItemName.text.toString().trim()
            if (name.isEmpty()) {
                binding.etItemName.error = getString(R.string.errorEmptyName)
                return@setOnClickListener
            }

            val year = binding.datePicker.year
            val month = binding.datePicker.month + 1   // DatePicker month is 0-indexed
            val day = binding.datePicker.dayOfMonth
            val date = String.format("%04d-%02d-%02d", year, month, day)

            val resultIntent = Intent().apply {
                putExtra(EXTRA_ITEM_NAME, name)
                putExtra(EXTRA_ITEM_DATE, date)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_NAME, binding.etItemName.text.toString())
        outState.putInt(KEY_YEAR, binding.datePicker.year)
        outState.putInt(KEY_MONTH, binding.datePicker.month)
        outState.putInt(KEY_DAY, binding.datePicker.dayOfMonth)
    }

    companion object {
        const val EXTRA_ITEM_NAME = "extra_item_name"
        const val EXTRA_ITEM_DATE = "extra_item_date"

        private const val KEY_NAME = "key_name"
        private const val KEY_YEAR = "key_year"
        private const val KEY_MONTH = "key_month"
        private const val KEY_DAY = "key_day"

        //New Intent Pattern — the ONLY way to create an Intent for this Activity.
        fun newIntent(context: Context): Intent {
            return Intent(context, AddItemActivity::class.java)
        }
    }
}
