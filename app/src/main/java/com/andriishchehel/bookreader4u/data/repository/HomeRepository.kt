package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.Book
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getLatestBooks(): Flow<List<Book>>
    fun getMysteryBooks(): Flow<List<Book>>
}