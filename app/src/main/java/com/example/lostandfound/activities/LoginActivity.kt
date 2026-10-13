package com.example.lostandfound.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.ActivityLoginBinding
import com.example.lostandfound.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val emailOrId = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (emailOrId.isEmpty()) {
                binding.tilEmail.error = "Please enter college email or student ID"
                return@setOnClickListener
            } else {
                binding.tilEmail.error = null
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Please enter password"
                return@setOnClickListener
            } else {
                binding.tilPassword.error = null
            }

            val user = dbHelper.loginUser(emailOrId, password)
            if (user != null) {
                sessionManager.saveLoginSession(user.id, user.name, user.email, user.studentId)
                Toast.makeText(this, "Welcome, ${user.name}! 👋", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials. Try demo login.", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnDemoLogin.setOnClickListener {
            val accounts = arrayOf(
                "👤 Jainik Bhandari (Student Owner - 22011011001)",
                "🔍 Aarav Sharma (Student Finder - 22011011015)",
                "🛡️ GCET Security Desk (Campus Staff)"
            )
            AlertDialog.Builder(this)
                .setTitle("⚡ Fast Demo Switcher")
                .setItems(accounts) { _, which ->
                    val user = when (which) {
                        0 -> dbHelper.getUserById(1) ?: dbHelper.loginUser("jainik.bhandari@gcet.ac.in", "password123")
                        1 -> dbHelper.getUserById(2) ?: dbHelper.loginUser("aarav.sharma@gcet.ac.in", "password123")
                        else -> dbHelper.getUserById(3) ?: dbHelper.loginUser("security@gcet.ac.in", "security123")
                    }

                    if (user != null) {
                        sessionManager.saveLoginSession(user.id, user.name, user.email, user.studentId)
                        Toast.makeText(this, "Logged in as ${user.name} ⚡", Toast.LENGTH_SHORT).show()
                    } else {
                        // Fallback defaults
                        when (which) {
                            0 -> sessionManager.saveLoginSession(1, "Jainik Bhandari", "jainik.bhandari@gcet.ac.in", "22011011001")
                            1 -> sessionManager.saveLoginSession(2, "Aarav Sharma", "aarav.sharma@gcet.ac.in", "22011011015")
                            else -> sessionManager.saveLoginSession(3, "GCET Security Desk", "security@gcet.ac.in", "GCET-STAFF-01")
                        }
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
                .show()
        }

        binding.tvForgotPassword.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("A reset link has been dispatched to your registered college email.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.tvSignUpPrompt.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
