package com.example.lostandfound.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.adapters.NotificationAdapter
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.ActivityNotificationsBinding
import com.example.lostandfound.utils.SessionManager

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var notifAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        setupRecyclerView()
        setupListeners()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        notifAdapter = NotificationAdapter(emptyList()) { notif ->
            if (notif.relatedItemId != null && notif.relatedItemId > 0) {
                if (notif.type == "match") {
                    val intent = Intent(this, SmartMatchActivity::class.java).apply {
                        putExtra("ITEM_ID", notif.relatedItemId)
                    }
                    startActivity(intent)
                } else if (notif.type.startsWith("claim")) {
                    val intent = Intent(this, ClaimDetailsActivity::class.java).apply {
                        putExtra("ITEM_ID", notif.relatedItemId)
                    }
                    startActivity(intent)
                } else {
                    val intent = Intent(this, ItemDetailsActivity::class.java).apply {
                        putExtra("ITEM_ID", notif.relatedItemId)
                    }
                    startActivity(intent)
                }
            }
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            adapter = notifAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.btnBackNotifs.setOnClickListener { finish() }

        binding.btnMarkAllRead.setOnClickListener {
            val userId = sessionManager.getUserId()
            dbHelper.markAllNotificationsRead(userId)
            Toast.makeText(this, "All notifications marked as read.", Toast.LENGTH_SHORT).show()
            loadNotifications()
        }
    }

    private fun loadNotifications() {
        val userId = sessionManager.getUserId()
        val notifs = dbHelper.getNotifications(userId)
        notifAdapter.updateList(notifs)

        if (notifs.isEmpty()) {
            binding.layoutEmptyNotifs.visibility = View.VISIBLE
            binding.rvNotifications.visibility = View.GONE
        } else {
            binding.layoutEmptyNotifs.visibility = View.GONE
            binding.rvNotifications.visibility = View.VISIBLE
        }
    }
}
