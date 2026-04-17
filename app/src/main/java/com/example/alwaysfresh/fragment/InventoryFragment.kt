package com.example.alwaysfresh.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alwaysfresh.AddItemActivity
import com.example.alwaysfresh.ItemDetailActivity
import com.example.alwaysfresh.adapter.ItemAdapter
import com.example.alwaysfresh.databinding.FragmentInventoryBinding
import com.example.alwaysfresh.viewmodel.MainViewModel

/**
 * Default fragment — displays the inventory summary and item list in a RecyclerView.
 * Shares MainViewModel with the parent Activity.
 */
class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private val addItemLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val name = data.getStringExtra(AddItemActivity.EXTRA_ITEM_NAME)
                ?: return@registerForActivityResult
            val date = data.getStringExtra(AddItemActivity.EXTRA_ITEM_DATE)
                ?: return@registerForActivityResult
            viewModel.addItem(name, date)
        }
    }

    private lateinit var adapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
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

        // RecyclerView
        adapter = ItemAdapter { item ->
            val intent = ItemDetailActivity.newIntent(requireContext(), item.id)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.displayItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.tvEmptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.fabAddItem.setOnClickListener {
            addItemLauncher.launch(AddItemActivity.newIntent(requireContext()))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
