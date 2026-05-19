package com.andriishchehel.bookreader4u.data.model

data class Review(
    var reviewerName: String,
    var reviewerAvatar: String,
    val rating: Float,
    val text: String = "",
    val date: String,
)
