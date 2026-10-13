package com.example.lostandfound.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.activities.ItemDetailsActivity
import com.example.lostandfound.activities.SmartMatchActivity
import com.example.lostandfound.adapters.MyReportAdapter
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.FragmentMyReportsBinding
import com.example.lostandfound.models.Item
import com.example.lostandfound.utils.SessionManager
import com.google.android.material.tabs.TabLayout

class MyReportsFragment : Fragment() {

    private var _binding: FragmentMyReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var reportAdapter: MyReportAdapter

    private var currentFilter: String = "ALL" // "ALL", "LOST", "FOUND", "RESOLVED"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupTabs()
        loadReports()
    }

    override fun onResume() {
        super.onResume()
        loadReports()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadReports()
        }
    }

    private fun setupRecyclerView() {
        reportAdapter = MyReportAdapter(
            reports = emptyList(),
            onItemClick = { item ->
                val intent = Intent(requireContext(), ItemDetailsActivity::class.java).apply {
                    putExtra("ITEM_ID", item.id)
                }
                startActivity(intent)
            },
            onResolveClick = { item ->
                confirmResolveItem(item)
            },
            onDeleteClick = { item ->
                confirmDeleteItem(item)
            },
            onMatchClick = { item ->
                val intent = Intent(requireContext(), SmartMatchActivity::class.java).apply {
                    putExtra("ITEM_ID", item.id)
                }
                startActivity(intent)
            }
        )

        binding.rvMyReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupTabs() {
        binding.tabLayoutMyReports.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = when (tab?.position) {
                    1 -> "LOST"
                    2 -> "FOUND"
                    3 -> "RESOLVED"
                    else -> "ALL"
                }
                loadReports()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadReports() {
        val userId = sessionManager.getUserId()
        val reports = dbHelper.getMyReports(userId, currentFilter)
        reportAdapter.updateList(reports)

        if (reports.isEmpty()) {
            binding.layoutEmptyMyReports.visibility = View.VISIBLE
            binding.rvMyReports.visibility = View.GONE
        } else {
            binding.layoutEmptyMyReports.visibility = View.GONE
            binding.rvMyReports.visibility = View.VISIBLE
        }
    }

    private fun confirmResolveItem(item: Item) {
        AlertDialog.Builder(requireContext())
            .setTitle("Mark as Resolved")
            .setMessage("Confirm that '${item.itemName}' has been recovered/returned?")
            .setPositiveButton("Yes, Resolved") { _, _ ->
                dbHelper.updateItemStatus(item.id, "Resolved")
                dbHelper.addNotification(
                    userId = sessionManager.getUserId(),
                    title = "Item Resolved! 🤝",
                    message = "'${item.itemName}' was successfully marked as resolved.",
                    type = "returned",
                    relatedItemId = item.id
                )
                Toast.makeText(requireContext(), "Item marked as resolved! 🎉", Toast.LENGTH_SHORT).show()
                loadReports()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteItem(item: Item) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Report")
            .setMessage("Are you sure you want to remove '${item.itemName}' from your reports?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteItem(item.id)
                Toast.makeText(requireContext(), "Report removed.", Toast.LENGTH_SHORT).show()
                loadReports()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
