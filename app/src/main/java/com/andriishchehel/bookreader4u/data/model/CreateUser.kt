package com.andriishchehel.bookreader4u.data.model

import com.google.firebase.Timestamp

data class CreateUser(
    val name: String,
    val email: String,
    val role: String = "user",
    val registeredAt: Timestamp = Timestamp.now(),
)
