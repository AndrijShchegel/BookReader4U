package com.andriishchehel.bookreader4u.data.model

data class ContinueBook(
    val bookId: String,
    val image: String,
    val title: String,
    val author: String,
    val fileUrl: String,
    val readingProgress: Int
)
