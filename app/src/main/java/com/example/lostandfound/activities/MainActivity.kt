package com.example.lostandfound.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityMainBinding
import com.example.lostandfound.fragments.ExploreFragment
import com.example.lostandfound.fragments.HomeFragment
import com.example.lostandfound.fragments.MyReportsFragment
import com.example.lostandfound.fragments.ProfileFragment
import com.example.lostandfound.fragments.ReportItemFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val exploreFragment = ExploreFragment()
    private val reportItemFragment = ReportItemFragment()
    private val myReportsFragment = MyReportsFragment()
    private val profileFragment = ProfileFragment()

    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFragments()
        setupBottomNav()
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, profileFragment, "PROFILE").hide(profileFragment)
            .add(R.id.fragmentContainer, myReportsFragment, "MY_REPORTS").hide(myReportsFragment)
            .add(R.id.fragmentContainer, reportItemFragment, "REPORT").hide(reportItemFragment)
            .add(R.id.fragmentContainer, exploreFragment, "EXPLORE").hide(exploreFragment)
            .add(R.id.fragmentContainer, homeFragment, "HOME")
            .commit()

        activeFragment = homeFragment
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchFragment(homeFragment)
                    true
                }
                R.id.nav_explore -> {
                    switchFragment(exploreFragment)
                    true
                }
                R.id.nav_report -> {
                    switchFragment(reportItemFragment)
                    true
                }
                R.id.nav_my_reports -> {
                    switchFragment(myReportsFragment)
                    true
                }
                R.id.nav_profile -> {
                    switchFragment(profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        if (activeFragment != targetFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(targetFragment)
                .commit()
            activeFragment = targetFragment
        }
    }

    fun navigateToExplore(category: String? = null, openFilterDialog: Boolean = false) {
        binding.bottomNavigation.selectedItemId = R.id.nav_explore
        if (category != null) {
            exploreFragment.applyCategory(category)
        }
        if (openFilterDialog) {
            exploreFragment.showFilterDialog()
        }
    }

    fun navigateToReport(defaultType: String) {
        reportItemFragment.setType(defaultType)
        binding.bottomNavigation.selectedItemId = R.id.nav_report
    }

    fun navigateToMyReports() {
        binding.bottomNavigation.selectedItemId = R.id.nav_my_reports
    }
}
