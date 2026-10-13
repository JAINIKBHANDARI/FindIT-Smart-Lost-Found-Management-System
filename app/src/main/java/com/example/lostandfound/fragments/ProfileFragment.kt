package com.example.lostandfound.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.lostandfound.activities.EditProfileActivity
import com.example.lostandfound.activities.LoginActivity
import com.example.lostandfound.activities.NotificationsActivity
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.FragmentProfileBinding
import com.example.lostandfound.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        setupUserData()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        setupUserData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setupUserData()
        }
    }

    private fun setupUserData() {
        val userId = sessionManager.getUserId()
        val user = dbHelper.getUserById(userId)

        val name = user?.name ?: sessionManager.getUserName()
        val email = user?.email ?: sessionManager.getUserEmail()
        val studentId = user?.studentId ?: sessionManager.getStudentId()
        val dept = user?.department?.ifBlank { "Computer Science & Engineering" } ?: "Computer Science & Engineering"

        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email
        binding.tvProfileId.text = "ID: $studentId • $dept"

        // Initials avatar
        val initials = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
        binding.tvProfileAvatarLetter.text = if (initials.isNotEmpty()) initials else "U"

        // Live statistics
        val stats = dbHelper.getUserStats(userId)
        binding.tvStatPosted.text = stats.first.toString()
        binding.tvStatFound.text = stats.second.toString()
        binding.tvStatReturned.text = stats.third.toString()
    }

    private fun setupListeners() {
        // Edit Profile
        binding.menuEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        // Notifications
        binding.menuNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }

        // Campus Security & Help Desk
        binding.menuHelp.setOnClickListener {
            showCampusHelpDialog()
        }

        // Fast Instant Logout (1-tap, zero delay, smooth transition)
        binding.btnLogout.setOnClickListener {
            val hostActivity = activity ?: return@setOnClickListener
            sessionManager.logout()
            Toast.makeText(hostActivity, "Signed out 👋", Toast.LENGTH_SHORT).show()
            val intent = Intent(hostActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            hostActivity.startActivity(intent)
            hostActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            hostActivity.finish()
        }
    }

    private fun showCampusHelpDialog() {
        val message = """
            🛡️ GCET Campus Security Desk
            📍 Location: GCET Main Entrance Gate #1
            ⏰ Hours: 24/7 Helpline & Safe Depository
            📞 Phone: +91 2692 231651
            ✉️ Email: security@gcet.ac.in
            
            Lost identity cards and sensitive valuables (wallets, electronics) are held at the GCET Central Security Vault for 30 days. Handover verification with College ID card is mandatory.
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("GCET Security & Help Desk")
            .setMessage(message)
            .setPositiveButton("Call Security") { _, _ ->
                try {
                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+912692231651"))
                    startActivity(callIntent)
                } catch (e: Exception) {
                    // Ignored if dialer not present
                }
            }
            .setNeutralButton("Email Desk") { _, _ ->
                try {
                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:security@gcet.ac.in")).apply {
                        putExtra(Intent.EXTRA_SUBJECT, "GCET Lost & Found Query")
                    }
                    startActivity(emailIntent)
                } catch (e: Exception) {
                    // Ignored
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
