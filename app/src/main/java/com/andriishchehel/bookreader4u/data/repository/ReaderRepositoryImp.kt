package com.andriishchehel.bookreader4u.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator
import java.io.File

class ReaderRepositoryImp(
    private val storage: FirebaseStorage,
    private val database: FirebaseFirestore,
    private val baseRepository: BaseRepository,
) : ReaderRepository {
    override suspend fun downloadBook(url: String, file: File): Result<Unit> = try {
        storage.getReferenceFromUrl(url).getFile(file).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveProgression(bookId: String, locator: Locator): Result<Unit> = try {
        val userId = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        val locatorString = locator.toJSON().toString()

        val data = mapOf("locator" to locatorString)

        baseRepository.setDocument("progressLocators", "${userId}_$bookId", data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun fetchProgression(bookId: String): Locator? {
        return try {
            val userId = baseRepository.fetchCurrentUserId().getOrElse {
                return null
            }
            val documentId = "${userId}_$bookId"
            val documentSnapshot =
                baseRepository.fetchDocument("progressLocators", documentId).getOrElse {
                    return null
                }

            val locatorJson = documentSnapshot.getString("locator") ?: return null

            val locator = Locator.fromJSON(JSONObject(locatorJson))
            locator
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setPreferences(preferences: Map<String, Any>): Result<Unit> = try {
        val userId = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        val updates = mapOf("preferences" to preferences)
        baseRepository.updateDocument("users", userId, updates)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getPreferencesFlow(): Flow<Map<String, Any>> = callbackFlow {
        val userId = baseRepository.fetchCurrentUserId().getOrElse {
            close(it)
            return@callbackFlow
        }
        val listenerRegistration =
            database.collection("users").document(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val preferences = snapshot.get("preferences") as? Map<String, Any>
                    if (preferences != null) {
                        trySend(preferences).isSuccess
                    }
                }
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }
}