package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.ContinueBook
import kotlinx.coroutines.flow.Flow

interface LibrarySavedRepository {
    fun getSavedBooks(): Flow<List<ContinueBook>>
}