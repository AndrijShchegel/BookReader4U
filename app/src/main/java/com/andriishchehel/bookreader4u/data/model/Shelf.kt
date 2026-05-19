package com.andriishchehel.bookreader4u.data.model

data class Shelf(
    val shelfId: String,
    val shelfName: String,
    val bookIds: List<String>
)
