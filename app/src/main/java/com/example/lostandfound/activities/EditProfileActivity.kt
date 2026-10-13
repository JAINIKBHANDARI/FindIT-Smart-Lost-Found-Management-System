package com.example.lostandfound.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.ActivityEditProfileBinding
import com.example.lostandfound.utils.SessionManager

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        loadUserData()
        setupListeners()
    }

    private fun loadUserData() {
        val user = dbHelper.getUserById(sessionManager.getUserId())

        binding.etEditName.setText(user?.name ?: sessionManager.getUserName())
        binding.etEditEmail.setText(user?.email ?: sessionManager.getUserEmail())
        binding.etEditStudentId.setText(user?.studentId ?: sessionManager.getStudentId())
        binding.etEditDepartment.setText(user?.department?.ifBlank { "Computer Science & Engineering" } ?: "Computer Science & Engineering")
        binding.etEditPhone.setText(user?.phone?.ifBlank { "+91 98250 12345" } ?: "+91 98250 12345")
    }

    private fun setupListeners() {
        binding.btnBackEditProfile.setOnClickListener { finish() }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etEditName.text.toString().trim()
            val email = binding.etEditEmail.text.toString().trim()
            val studentId = binding.etEditStudentId.text.toString().trim()
            val department = binding.etEditDepartment.text.toString().trim()
            val phone = binding.etEditPhone.text.toString().trim()

            if (name.isEmpty()) {
                binding.etEditName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                binding.etEditEmail.error = "Email cannot be empty"
                return@setOnClickListener
            }

            val currentUserId = sessionManager.getUserId()
            val existingUser = dbHelper.getUserById(currentUserId)
            if (existingUser != null) {
                existingUser.name = name
                existingUser.email = email
                existingUser.studentId = studentId
                existingUser.department = department
                existingUser.phone = phone

                dbHelper.updateUser(existingUser)
                sessionManager.saveLoginSession(currentUserId, name, email, studentId)

                Toast.makeText(this, "Profile updated successfully! ✨", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
