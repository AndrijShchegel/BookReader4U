package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.ContinueBook
import com.andriishchehel.bookreader4u.data.model.Shelfs
import kotlinx.coroutines.flow.Flow

interface ShelfRepository {
    fun fetchShelfs(): Flow<List<Shelfs>>
    suspend fun createShelf(name: String): Result<Unit>
    suspend fun updateShelfName(shelfId: String, name: String): Result<Unit>
    suspend fun deleteShelf(shelfId: String): Result<Unit>
    fun fetchShelfBooks(shelfId: String): Flow<List<ContinueBook>>
    suspend fun removeBookFromShelf(bookId: String, shelfId: String): Result<Unit>
    suspend fun addBooksToShelf(bookIds: List<String>, shelfId: String): Result<Unit>
    suspend fun fetchAvailableBooks(shelfId: String): Result<List<ContinueBook>>
}