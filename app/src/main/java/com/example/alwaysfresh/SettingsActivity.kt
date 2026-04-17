package com.example.alwaysfresh

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.alwaysfresh.data.AppDatabase
import com.example.alwaysfresh.databinding.ActivitySettingsBinding
import com.example.alwaysfresh.model.InventoryRepository
import kotlinx.coroutines.launch

/**
 * VIEW — Settings screen with dark mode toggle and database wipe option.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var repository: InventoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getInstance(application).itemDao()
        repository = InventoryRepository(dao)

        binding.btnBack.setOnClickListener { finish() }

        setupDarkMode()
        setupWipeDatabase()
    }

    private fun setupDarkMode() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isDark = prefs.getBoolean(KEY_DARK_MODE, false)
        binding.switchDarkMode.isChecked = isDark

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupWipeDatabase() {
        binding.btnWipeData.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.wipe_confirm_title)
                .setMessage(R.string.wipe_confirm_message)
                .setPositiveButton(R.string.wipe_database) { _, _ ->
                    lifecycleScope.launch {
                        repository.deleteAll()
                        Toast.makeText(
                            this@SettingsActivity,
                            R.string.wipe_success,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    companion object {
        const val PREFS_NAME = "alwaysfresh_settings"
        const val KEY_DARK_MODE = "dark_mode"
    }
}
