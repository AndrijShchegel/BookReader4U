package com.andriishchehel.bookreader4u.data.model

data class Shelfs(
    val shelfId: String,
    val shelfName: String,
    val bookIds: List<String>,
    val firstBookImage: String
)
