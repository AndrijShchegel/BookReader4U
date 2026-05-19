package com.andriishchehel.bookreader4u.data.model

import com.google.firebase.Timestamp

data class CreateReview(
    val reviewFrom: String,
    val reviewTo: String,
    val reviewScore: Float,
    val reviewText: String = "",
    val timestamp: Timestamp,
)
