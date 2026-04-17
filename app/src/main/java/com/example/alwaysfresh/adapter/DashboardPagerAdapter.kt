package com.example.alwaysfresh.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.alwaysfresh.fragment.ShoppingListFragment
import com.example.alwaysfresh.fragment.WasteAnalyticsFragment

/**
 * ViewPager2 adapter for the Dashboard tabs.
 * Hosts ShoppingListFragment and WasteAnalyticsFragment.
 */
class DashboardPagerAdapter(parentFragment: Fragment) : FragmentStateAdapter(parentFragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ShoppingListFragment()
        1 -> WasteAnalyticsFragment()
        else -> throw IllegalArgumentException("Invalid tab position: $position")
    }
}
