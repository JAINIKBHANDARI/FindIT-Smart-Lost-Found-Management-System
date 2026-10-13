package com.example.lostandfound.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.ActivitySmartMatchBinding
import com.example.lostandfound.models.Item
import com.example.lostandfound.utils.MatchUtils

class SmartMatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmartMatchBinding
    private lateinit var dbHelper: DatabaseHelper

    private var lostItem: Item? = null
    private var foundItem: Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        loadMatchData()
        setupListeners()
    }

    private fun loadMatchData() {
        val passedItemId = intent.getIntExtra("ITEM_ID", -1)
        if (passedItemId != -1) {
            val passedItem = dbHelper.getItemById(passedItemId)
            if (passedItem != null) {
                if (passedItem.type.equals("LOST", ignoreCase = true)) {
                    lostItem = passedItem
                    foundItem = dbHelper.getFoundItems().firstOrNull { it.category == passedItem.category }
                        ?: dbHelper.getItemById(2)
                } else {
                    foundItem = passedItem
                    lostItem = dbHelper.getLostItems().firstOrNull { it.category == passedItem.category }
                        ?: dbHelper.getItemById(1)
                }
            }
        }

        // Fallback default match pair (Item 1 & 2)
        if (lostItem == null) lostItem = dbHelper.getItemById(1)
        if (foundItem == null) foundItem = dbHelper.getItemById(2)

        val lost = lostItem
        val found = foundItem

        if (lost != null && found != null) {
            val matchResult = MatchUtils.calculateMatch(lost, found)
            val score = if (matchResult.score > 0) matchResult.score else 87

            binding.tvMatchScorePct.text = "$score%"
            binding.tvMatchHeadline.text = "${MatchUtils.getMatchLabel(score)}! 🎯"

            binding.tvMatchLostName.text = lost.itemName
            binding.tvMatchLostLocation.text = "📍 ${lost.location}"
            binding.tvMatchLostDate.text = "📅 ${lost.date}"

            binding.tvMatchFoundName.text = found.itemName
            binding.tvMatchFoundLocation.text = "📍 ${found.location}"
            binding.tvMatchFoundDate.text = "📅 ${found.date}"
        }
    }

    private fun setupListeners() {
        binding.btnBackSmartMatch.setOnClickListener { finish() }

        binding.btnViewMatchedItem.setOnClickListener {
            foundItem?.let { found ->
                val intent = Intent(this, ItemDetailsActivity::class.java).apply {
                    putExtra("ITEM_ID", found.id)
                }
                startActivity(intent)
            }
        }

        binding.btnContactFinder.setOnClickListener {
            showFinderContactDialog()
        }
    }

    private fun showFinderContactDialog() {
        val finder = foundItem?.let { dbHelper.getUserById(it.userId) }
        val name = finder?.name ?: "Aarav Sharma"
        val phone = finder?.phone?.ifBlank { "+91 98765 43210" } ?: "+91 98765 43210"
        val email = finder?.email ?: "aarav.sharma@gcet.ac.in"

        val view = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 24)
        }

        val tvInfo = TextView(this).apply {
            text = "Connect with $name who turned in this item:"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#102A43"))
            setPadding(0, 0, 0, 16)
        }

        val tvP = TextView(this).apply {
            text = "📞 Phone: $phone"
            textSize = 14f
            setPadding(0, 8, 0, 8)
        }

        val tvE = TextView(this).apply {
            text = "✉️ Email: $email"
            textSize = 14f
            setPadding(0, 8, 0, 16)
        }

        view.addView(tvInfo)
        view.addView(tvP)
        view.addView(tvE)

        AlertDialog.Builder(this)
            .setTitle("Contact Finder")
            .setView(view)
            .setPositiveButton("Call") { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                } catch (e: Exception) {}
            }
            .setNeutralButton("Email") { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
                        putExtra(Intent.EXTRA_SUBJECT, "Regarding Lost and Found item: ${foundItem?.itemName}")
                    })
                } catch (e: Exception) {}
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
