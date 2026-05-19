package com.andriishchehel.bookreader4u.data.model

import com.google.firebase.Timestamp

data class User(
    val name : String,
    val email : String,
    val profileImage: String,
    val role: String,
    val registeredAt : Timestamp,
    val reviewIds: List<String>,
    val savedBooksIds: List<String>,
)
