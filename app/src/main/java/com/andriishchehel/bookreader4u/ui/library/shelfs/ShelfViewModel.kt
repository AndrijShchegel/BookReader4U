package com.andriishchehel.bookreader4u.ui.library.shelfs

import androidx.lifecycle.ViewModel
import com.andriishchehel.bookreader4u.data.model.ContinueBook
import com.andriishchehel.bookreader4u.data.model.Shelfs
import com.andriishchehel.bookreader4u.data.repository.ShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ShelfViewModel @Inject constructor(
    private val repository: ShelfRepository
) : ViewModel() {

    fun fetchShelfs(): Flow<List<Shelfs>> {
        return repository.fetchShelfs()
    }

    suspend fun createShelf(name: String): Result<Unit> {
        if (name.isEmpty()) {
            return Result.failure(IllegalArgumentException("Назва полиці не може бути порожньою"))
        }
        return repository.createShelf(name)
    }

    suspend fun updateShelfName(shelfId: String, name: String): Result<Unit> {
        if (name.isEmpty()) {
            return Result.failure(IllegalArgumentException("Назва полиці не може бути порожньою"))
        }
        return repository.updateShelfName(shelfId, name)
    }

    suspend fun deleteShelf(shelfId: String): Result<Unit> {
        return repository.deleteShelf(shelfId)
    }

    fun fetchShelfBooks(shelfId: String): Flow<List<ContinueBook>> {
        return repository.fetchShelfBooks(shelfId)
    }

    suspend fun removeBookFromShelf(bookId: String, shelfId: String): Result<Unit> {
        return repository.removeBookFromShelf(bookId, shelfId)
    }

    suspend fun addBooksToShelf(bookIds: List<String>, shelfId: String): Result<Unit> {
        return repository.addBooksToShelf(bookIds, shelfId)
    }

    suspend fun fetchReadingBooks(shelfId: String): Result<List<ContinueBook>> {
        return repository.fetchAvailableBooks(shelfId)
    }
}