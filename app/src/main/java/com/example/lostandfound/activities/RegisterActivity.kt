package com.example.lostandfound.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.ActivityRegisterBinding
import com.example.lostandfound.models.User
import com.example.lostandfound.utils.SessionManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etRegName.text.toString().trim()
            val email = binding.etRegEmail.text.toString().trim()
            val studentId = binding.etRegStudentId.text.toString().trim()
            val department = binding.etRegDepartment.text.toString().trim()
            val password = binding.etRegPassword.text.toString().trim()
            val confirmPass = binding.etRegConfirmPassword.text.toString().trim()
            val termsAccepted = binding.cbTerms.isChecked

            if (name.isEmpty() || email.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!termsAccepted) {
                Toast.makeText(this, "Please agree to the Campus Honor Code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(
                name = name,
                studentId = studentId,
                email = email,
                password = password,
                department = department.ifBlank { "Engineering Department" }
            )

            val id = dbHelper.registerUser(newUser)
            if (id > 0) {
                sessionManager.saveLoginSession(id.toInt(), name, email, studentId)
                Toast.makeText(this, "Account created successfully! 🎓", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Registration failed. Email or Student ID may already exist.", Toast.LENGTH_LONG).show()
            }
        }

        binding.tvSignInPrompt.setOnClickListener {
            finish()
        }
    }
}
