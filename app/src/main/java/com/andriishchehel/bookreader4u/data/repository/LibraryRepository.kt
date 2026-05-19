package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.ContinueBook
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun fetchSavedBookSize(): Flow<Int>
    fun fetchShelfsSize(): Flow<Int>
    fun fetchReading(): Flow<List<ContinueBook>>
}