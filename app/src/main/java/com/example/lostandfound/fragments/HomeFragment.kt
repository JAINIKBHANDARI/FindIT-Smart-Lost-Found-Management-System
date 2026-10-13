package com.example.lostandfound.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.activities.ItemDetailsActivity
import com.example.lostandfound.activities.MainActivity
import com.example.lostandfound.activities.NotificationsActivity
import com.example.lostandfound.activities.SmartMatchActivity
import com.example.lostandfound.adapters.CategoryAdapter
import com.example.lostandfound.adapters.ItemAdapter
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.FragmentHomeBinding
import com.example.lostandfound.models.Item
import com.example.lostandfound.utils.SessionManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recentItemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        setupGreeting()
        setupRecyclerViews()
        setupListeners()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        setupGreeting()
        loadData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setupGreeting()
            loadData()
        }
    }

    private fun setupGreeting() {
        val userName = sessionManager.getUserName().split(" ").firstOrNull() ?: sessionManager.getUserName()
        binding.tvGreeting.text = "Hello, $userName 👋"
    }

    private fun setupRecyclerViews() {
        // Categories Horizontal
        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            (activity as? MainActivity)?.navigateToExplore(category = if (category.name == "All") null else category.name)
        }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }

        // Recent Reports Vertical
        recentItemAdapter = ItemAdapter(emptyList()) { item ->
            val intent = Intent(requireContext(), ItemDetailsActivity::class.java).apply {
                putExtra("ITEM_ID", item.id)
            }
            startActivity(intent)
        }
        binding.rvRecentReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentItemAdapter
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        // Notifications Button
        binding.btnNotificationHeader.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }

        // Smart Match Alert Card
        binding.cardSmartMatchAlert.setOnClickListener {
            startActivity(Intent(requireContext(), SmartMatchActivity::class.java))
        }

        // Search Bar click -> navigate to explore
        binding.cardSearchBar.setOnClickListener {
            (activity as? MainActivity)?.navigateToExplore()
        }

        // Filter button click -> navigate to explore with filter
        binding.btnSearchFilter.setOnClickListener {
            (activity as? MainActivity)?.navigateToExplore(openFilterDialog = true)
        }

        // Quick Actions
        binding.cardReportLost.setOnClickListener {
            (activity as? MainActivity)?.navigateToReport(defaultType = "LOST")
        }

        binding.cardReportFound.setOnClickListener {
            (activity as? MainActivity)?.navigateToReport(defaultType = "FOUND")
        }

        // See All Recent
        binding.tvSeeAllRecent.setOnClickListener {
            (activity as? MainActivity)?.navigateToExplore()
        }
    }

    private fun loadData() {
        // Notification Badge Dot
        val unreadCount = dbHelper.getUnreadNotificationCount(sessionManager.getUserId())
        binding.viewNotifBadge.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE

        // Categories
        val categories = dbHelper.getCategoriesWithCounts()
        categoryAdapter.updateData(categories)

        // Recent Reports
        val recentItems = dbHelper.getRecentItems(8)
        recentItemAdapter.updateList(recentItems)

        // Smart Match alert banner update
        val potentialMatches = dbHelper.searchAndFilterItems(null, null, null, null)
            .filter { it.status.equals("Potential Match", ignoreCase = true) }

        if (potentialMatches.isNotEmpty()) {
            val topMatch = potentialMatches.first()
            binding.tvAlertTitle.text = "Possible match for ${topMatch.itemName}!"
            binding.cardSmartMatchAlert.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
