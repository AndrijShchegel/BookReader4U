package com.andriishchehel.bookreader4u.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BaseRepositoryImp(
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : BaseRepository {

    override fun fetchCurrentUserId(): Result<String> {
        return auth.currentUser?.uid?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("User not logged in"))
    }

    override suspend fun createDocument(
        collectionId: String,
        data: Any
    ): Result<String> = try {
        val reviewRef = database.collection(collectionId).add(data).await()
        Result.success(reviewRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun fetchDocument(
        collectionId: String,
        documentId: String
    ): Result<DocumentSnapshot> = try {
        Result.success(database.collection(collectionId).document(documentId).get().await())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateDocument(
        collectionId: String,
        documentId: String,
        updates: Map<String, Any>
    ): Result<Unit> = try {
        database.collection(collectionId).document(documentId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteDocument(
        collectionId: String,
        documentId: String
    ): Result<Unit> = try {
        database.collection(collectionId).document(documentId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun setDocument(
        collectionId: String,
        documentId: String,
        data: Any
    ): Result<Unit> = try {
        database.collection(collectionId).document(documentId).set(data).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}