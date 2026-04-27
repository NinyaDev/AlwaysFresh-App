package com.example.alwaysfresh

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.alwaysfresh.databinding.ActivityMainBinding
import com.example.alwaysfresh.fragment.DashboardFragment
import com.example.alwaysfresh.fragment.InventoryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigationrail.NavigationRailView

/**
 * VIEW — Host container for fragments.
 * Portrait uses BottomNavigationView, landscape uses NavigationRailView.
 * Settings tab launches SettingsActivity.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentNavId = R.id.nav_inventory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restore the selected tab after config change, or default to Inventory.
        // Fragments themselves are restored automatically by the FragmentManager;
        // we only do the initial add on first launch.
        if (savedInstanceState != null) {
            currentNavId = savedInstanceState.getInt(KEY_NAV_ID, R.id.nav_inventory)
        } else {
            val dashboard = DashboardFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, InventoryFragment(), TAG_INVENTORY)
                .add(R.id.fragmentContainer, dashboard, TAG_DASHBOARD)
                .hide(dashboard)
                .commit()
        }

        // Find whichever nav exists — BottomNav (portrait) or NavRail (landscape)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val navRail = findViewById<NavigationRailView>(R.id.navRail)
        val navView: NavigationBarView = bottomNav ?: navRail ?: return

        navView.selectedItemId = currentNavId

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inventory -> {
                    currentNavId = R.id.nav_inventory
                    showFragment(TAG_INVENTORY)
                    true
                }
                R.id.nav_dashboard -> {
                    currentNavId = R.id.nav_dashboard
                    showFragment(TAG_DASHBOARD)
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_NAV_ID, currentNavId)
    }

    private fun showFragment(tag: String) {
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()

        fm.findFragmentByTag(TAG_INVENTORY)?.let { transaction.hide(it) }
        fm.findFragmentByTag(TAG_DASHBOARD)?.let { transaction.hide(it) }
        fm.findFragmentByTag(tag)?.let { transaction.show(it) }

        transaction.commit()
    }

    companion object {
        private const val TAG_INVENTORY = "inventory"
        private const val TAG_DASHBOARD = "dashboard"
        private const val KEY_NAV_ID = "key_nav_id"
    }
}
