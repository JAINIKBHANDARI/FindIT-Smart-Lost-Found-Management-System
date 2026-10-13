package com.example.lostandfound.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "LostAndFoundSession"
        private const val IS_LOGGED_IN = "IsLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_NAME = "userName"
        private const val KEY_EMAIL = "userEmail"
        private const val KEY_STUDENT_ID = "studentId"
        private const val KEY_DARK_MODE = "darkMode"
    }

    fun saveLoginSession(userId: Int, name: String, email: String, studentId: String = "") {
        pref.edit()
            .putBoolean(IS_LOGGED_IN, true)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_STUDENT_ID, studentId)
            .apply()
    }

    fun isLoggedIn(): Boolean = pref.getBoolean(IS_LOGGED_IN, false)

    fun getUserId(): Int = pref.getInt(KEY_USER_ID, -1)

    fun getUserName(): String = pref.getString(KEY_NAME, "Jainik Bhandari") ?: "Jainik Bhandari"

    fun getUserEmail(): String = pref.getString(KEY_EMAIL, "jainik.bhandari@gcet.ac.in") ?: "jainik.bhandari@gcet.ac.in"

    fun getStudentId(): String = pref.getString(KEY_STUDENT_ID, "22011011001") ?: "22011011001"

    fun isDarkMode(): Boolean = pref.getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) {
        pref.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun logout() {
        pref.edit().clear().apply()
    }
}
