package com.example.alwaysfresh.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alwaysfresh.adapter.ShoppingListAdapter
import com.example.alwaysfresh.databinding.FragmentShoppingListBinding
import com.example.alwaysfresh.viewmodel.MainViewModel

/**
 * Nested fragment inside DashboardFragment's ViewPager2.
 * Shows items that were soft-deleted (shopping list / buy-again suggestions).
 */
class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var adapter: ShoppingListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ShoppingListAdapter()
        binding.recyclerViewShopping.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewShopping.adapter = adapter

        viewModel.deletedItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.tvEmptyShoppingList.visibility =
                if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewShopping.visibility =
                if (items.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
