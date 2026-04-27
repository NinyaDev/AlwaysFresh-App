package com.example.alwaysfresh

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * Applies the saved dark-mode preference once per process so every activity
 * — not just MainActivity — picks it up at launch.
 */
class AlwaysFreshApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE)
        val isDark = prefs.getBoolean(SettingsActivity.KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
