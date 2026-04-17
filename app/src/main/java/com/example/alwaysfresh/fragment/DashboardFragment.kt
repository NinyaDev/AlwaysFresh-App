package com.example.alwaysfresh.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.alwaysfresh.adapter.DashboardPagerAdapter
import com.example.alwaysfresh.databinding.FragmentDashboardBinding
import com.example.alwaysfresh.viewmodel.MainViewModel
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Dashboard fragment — shows inventory summary counts and a
 * ViewPager2 with ShoppingList and WasteAnalytics tabs.
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Summary counts
        viewModel.freshCount.observe(viewLifecycleOwner) { count ->
            binding.tvFresh.text = count.toString()
        }
        viewModel.expiringSoonCount.observe(viewLifecycleOwner) { count ->
            binding.tvExpiringSoon.text = count.toString()
        }
        viewModel.expiredCount.observe(viewLifecycleOwner) { count ->
            binding.tvExpired.text = count.toString()
        }

        // ViewPager2 + TabLayout
        val pagerAdapter = DashboardPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        val tabTitles = listOf(
            getString(com.example.alwaysfresh.R.string.shopping_list_tab),
            getString(com.example.alwaysfresh.R.string.waste_analytics_tab)
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
