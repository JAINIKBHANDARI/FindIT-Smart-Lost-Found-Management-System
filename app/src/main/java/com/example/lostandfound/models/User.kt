package com.example.lostandfound.models

import java.io.Serializable

data class User(
    var id: Int = 0,
    var name: String = "",
    var studentId: String = "",
    var email: String = "",
    var password: String = "",
    var phone: String = "",
    var department: String = "",
    var profileImage: String = ""
) : Serializable
