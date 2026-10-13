package com.example.lostandfound.models

data class NotificationItem(
    val id: Int = 0,
    val title: String,
    val message: String,
    val timeAgo: String,
    val type: String, // "match", "response", "claim", "returned", "system"
    var isRead: Boolean = false,
    val relatedItemId: Int? = null
)
