package com.andriishchehel.bookreader4u.data.model

import com.google.firebase.Timestamp

data class CreateBook(
    val image: String,
    val title: String,
    val author: String,
    val description: String,
    val rating: Long = 0L,
    val genres: List<String>,
    val starRatings: Map<String, Long>,
    val reviewCount: Long = 0L,
    val createAt: Timestamp,
)
