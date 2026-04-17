package com.example.alwaysfresh.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.alwaysfresh.databinding.FragmentWasteAnalyticsBinding
import com.example.alwaysfresh.viewmodel.MainViewModel

/**
 * Nested fragment inside DashboardFragment's ViewPager2.
 * Shows waste analytics: counts of items by status category.
 * Price-based analytics will be added later.
 */
class WasteAnalyticsFragment : Fragment() {

    private var _binding: FragmentWasteAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWasteAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.freshCount.observe(viewLifecycleOwner) { count ->
            binding.tvFreshCount.text = count.toString()
        }
        viewModel.expiringSoonCount.observe(viewLifecycleOwner) { count ->
            binding.tvExpiringCount.text = count.toString()
        }
        viewModel.expiredCount.observe(viewLifecycleOwner) { count ->
            binding.tvExpiredCount.text = count.toString()
        }
        viewModel.deletedItemCount.observe(viewLifecycleOwner) { count ->
            binding.tvWastedCount.text = count.toString()
        }
        viewModel.totalItemCount.observe(viewLifecycleOwner) { count ->
            binding.tvTotalTracked.text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
