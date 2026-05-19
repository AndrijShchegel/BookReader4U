package com.andriishchehel.bookreader4u.data.repository

import com.google.firebase.firestore.DocumentSnapshot

interface BaseRepository {

    fun fetchCurrentUserId(): Result<String>

    suspend fun createDocument(collectionId: String, data: Any): Result<String>

    suspend fun fetchDocument(collectionId: String, documentId: String): Result<DocumentSnapshot>

    suspend fun updateDocument(
        collectionId: String,
        documentId: String,
        updates: Map<String, Any>
    ): Result<Unit>

    suspend fun deleteDocument(collectionId: String, documentId: String): Result<Unit>

    suspend fun setDocument(
        collectionId: String,
        documentId: String,
        data: Any
    ): Result<Unit>
}