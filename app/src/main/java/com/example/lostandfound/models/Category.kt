package com.example.lostandfound.models

data class Category(
    val name: String,
    val iconEmoji: String,
    val count: Int = 0,
    var isSelected: Boolean = false
)
