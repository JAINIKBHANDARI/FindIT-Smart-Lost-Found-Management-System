package com.example.lostandfound.models

import java.io.Serializable

data class Item(
    var id: Int = 0,
    var userId: Int = 0,
    var type: String = "LOST", // "LOST" or "FOUND"
    var itemName: String = "",
    var category: String = "",
    var description: String = "",
    var brand: String = "",
    var color: String = "",
    var location: String = "",
    var locationDetail: String = "",
    var date: String = "",
    var time: String = "",
    var imageUri: String = "",
    var additionalInfo: String = "",
    var verificationQuestion: String = "",
    var verificationAnswer: String = "",
    var status: String = "Active", // "Active", "Potential Match", "Claim Pending", "Resolved"
    var createdAt: String = ""
) : Serializable {
    fun isSafeHandover(): Boolean = additionalInfo.contains("SAFE_HANDOVER")

    fun getSecurityPin(): String {
        val seed = Math.abs((id * 31415 + 9265) % 9000) + 1000
        return String.format("%04d", seed)
    }
}
