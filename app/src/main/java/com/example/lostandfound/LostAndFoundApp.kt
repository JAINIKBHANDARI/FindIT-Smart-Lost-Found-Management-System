package com.example.lostandfound

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class LostAndFoundApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Force Light mode globally so system dark mode does not invert inputs or cause white-on-white text issues
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
