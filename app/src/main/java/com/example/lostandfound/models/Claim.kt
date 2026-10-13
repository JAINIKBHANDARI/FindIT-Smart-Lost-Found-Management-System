package com.example.lostandfound.models

import java.io.Serializable

data class Claim(
    var id: Int = 0,
    var itemId: Int = 0,
    var claimantId: Int = 0,
    var claimantName: String = "",
    var claimantCollegeId: String = "",
    var claimantEmail: String = "",
    var reason: String = "",
    var verificationAnswer: String = "",
    var status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    var createdAt: String = "",
    var itemName: String = ""
) : Serializable
