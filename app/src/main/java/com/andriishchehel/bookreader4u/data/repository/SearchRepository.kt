package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.Book

interface SearchRepository {
    suspend fun search(
        orderBy: String,
        descendingOrder: Boolean,
        search: String,
        minRating: Float,
        useAndLogic: Boolean,
        included: List<String>,
        excluded: List<String>,
    ): List<Book>
}