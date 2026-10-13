package com.example.lostandfound.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.activities.ItemDetailsActivity
import com.example.lostandfound.adapters.ItemAdapter
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.FragmentExploreBinding
import com.example.lostandfound.models.Item
import java.util.Locale

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var itemAdapter: ItemAdapter

    private var allItems: List<Item> = emptyList()
    private var selectedType: String = "ALL" // "ALL", "LOST", "FOUND"
    private var selectedCategory: String = "All"
    private var selectedLocation: String = "All"
    private var currentQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        setupRecyclerView()
        setupSearch()
        setupChipGroup()
        setupFilterButton()
        loadItems()
    }

    override fun onResume() {
        super.onResume()
        loadItems()
    }

    fun applyCategory(category: String?) {
        selectedCategory = category ?: "All"
        if (isAdded && _binding != null) {
            applyFilters()
        }
    }

    fun showFilterDialog() {
        if (!isAdded) return

        val context = requireContext()
        val categories = listOf("All", "Electronics", "Keys", "ID Cards", "Bags", "Documents", "Clothing", "Books", "Other")
        val locations = dbHelper.getAllLocations()

        val view = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val tvCat = android.widget.TextView(context).apply {
            text = "Filter by Category:"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#102A43"))
            setPadding(0, 16, 0, 8)
        }
        val spCategory = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, categories)
            val pos = categories.indexOf(selectedCategory).coerceAtLeast(0)
            setSelection(pos)
        }

        val tvLoc = android.widget.TextView(context).apply {
            text = "Filter by Location:"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#102A43"))
            setPadding(0, 24, 0, 8)
        }
        val spLocation = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, locations)
            val pos = locations.indexOf(selectedLocation).coerceAtLeast(0)
            setSelection(pos)
        }

        view.addView(tvCat)
        view.addView(spCategory)
        view.addView(tvLoc)
        view.addView(spLocation)

        AlertDialog.Builder(context)
            .setTitle("Filter Explore Items")
            .setView(view)
            .setPositiveButton("Apply") { _, _ ->
                selectedCategory = spCategory.selectedItem?.toString() ?: "All"
                selectedLocation = spLocation.selectedItem?.toString() ?: "All"
                applyFilters()
            }
            .setNeutralButton("Reset") { _, _ ->
                selectedCategory = "All"
                selectedLocation = "All"
                applyFilters()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter(emptyList()) { item ->
            val intent = Intent(requireContext(), ItemDetailsActivity::class.java).apply {
                putExtra("ITEM_ID", item.id)
            }
            startActivity(intent)
        }

        binding.rvExploreItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearch() {
        binding.etExploreSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupChipGroup() {
        binding.chipAll.setOnClickListener {
            selectedType = "ALL"
            applyFilters()
        }
        binding.chipLost.setOnClickListener {
            selectedType = "LOST"
            applyFilters()
        }
        binding.chipFound.setOnClickListener {
            selectedType = "FOUND"
            applyFilters()
        }
    }

    private fun setupFilterButton() {
        binding.btnFilterOptions.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun loadItems() {
        allItems = dbHelper.getAllItems()
        applyFilters()
    }

    private fun applyFilters() {
        val query = currentQuery.lowercase(Locale.ROOT)

        val filtered = allItems.filter { item ->
            // Type filter
            val matchesType = when (selectedType) {
                "LOST" -> item.type.equals("LOST", ignoreCase = true)
                "FOUND" -> item.type.equals("FOUND", ignoreCase = true)
                else -> true
            }

            // Category filter
            val matchesCategory = if (selectedCategory == "All") true else item.category.equals(selectedCategory, ignoreCase = true)

            // Location filter
            val matchesLocation = if (selectedLocation == "All") true else item.location.contains(selectedLocation, ignoreCase = true)

            // Search query filter
            val matchesQuery = if (query.isEmpty()) {
                true
            } else {
                item.itemName.lowercase(Locale.ROOT).contains(query) ||
                item.description.lowercase(Locale.ROOT).contains(query) ||
                item.location.lowercase(Locale.ROOT).contains(query) ||
                item.brand.lowercase(Locale.ROOT).contains(query) ||
                item.category.lowercase(Locale.ROOT).contains(query)
            }

            matchesType && matchesCategory && matchesLocation && matchesQuery
        }

        itemAdapter.updateList(filtered)

        // Update count & empty state
        val count = filtered.size
        binding.tvExploreCount.text = "Showing $count items"

        if (count == 0) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvExploreItems.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvExploreItems.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
