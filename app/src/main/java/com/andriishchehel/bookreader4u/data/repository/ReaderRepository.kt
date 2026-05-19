package com.andriishchehel.bookreader4u.data.repository

import kotlinx.coroutines.flow.Flow
import org.readium.r2.shared.publication.Locator
import java.io.File

interface ReaderRepository {
    suspend fun downloadBook(url: String, file: File): Result<Unit>
    suspend fun saveProgression(bookId: String, locator: Locator): Result<Unit>
    suspend fun fetchProgression(bookId: String): Locator?
    suspend fun setPreferences(preferences: Map<String, Any>): Result<Unit>
    fun getPreferencesFlow(): Flow<Map<String, Any>>
}