package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.ContinueBook
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator

class LibraryRepositoryImp(
    private val database: FirebaseFirestore,
    private val baseRepository: BaseRepository
) : LibraryRepository {
    override fun fetchSavedBookSize(): Flow<Int> = flow {
        val uid = baseRepository.fetchCurrentUserId().getOrElse {
            emit(0)
            return@flow
        }
        val userDoc = baseRepository.fetchDocument("users", uid).getOrElse {
            emit(0)
            return@flow
        }
        val resultList = userDoc.get("savedBookIds") as? List<*> ?: emptyList<Any>()
        val savedBookIds = resultList.filterIsInstance<String>()
        emit(savedBookIds.size)
    }

    override fun fetchShelfsSize(): Flow<Int> = flow {
        val uid = baseRepository.fetchCurrentUserId().getOrElse {
            emit(0)
            return@flow
        }
        val shelfsSnapshot =
            database.collection("users").document(uid).collection("shelfs").get().await()
        emit(shelfsSnapshot.size())
    }

    override fun fetchReading(): Flow<List<ContinueBook>> = callbackFlow {
        val uid = baseRepository.fetchCurrentUserId().getOrElse {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val docRef = database.collection("users").document(uid)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val readingList = snapshot?.get("readingIds") as? List<*> ?: emptyList<Any>()
            val readingIds = readingList.filterIsInstance<String>()

            launch {
                val books = readingIds.mapNotNull { id ->
                    baseRepository.fetchDocument("books", id).getOrNull()?.let { doc ->
                        parseBook(uid, doc)
                    }
                }
                trySend(books)
            }
        }

        awaitClose { listener.remove() }
    }


    private suspend fun parseBook(uid: String, bookDoc: DocumentSnapshot): ContinueBook {
        val locatorDoc =
            baseRepository.fetchDocument("progressLocators", "${uid}_${bookDoc.id}").getOrNull()
        val readingProgress = try {
            val locatorString = locatorDoc?.getString("locator") ?: ""
            if (locatorString.isNotEmpty()) {
                val locator = Locator.fromJSON(JSONObject(locatorString))
                locator?.locations?.totalProgression?.times(100)?.toInt() ?: 0
            } else 0
        } catch (e: Exception) {
            0
        }

        return ContinueBook(
            bookId = bookDoc.id,
            image = bookDoc.getString("image") ?: "",
            title = bookDoc.getString("title") ?: "",
            author = bookDoc.getString("author") ?: "",
            fileUrl = bookDoc.getString("fileUrl") ?: "",
            readingProgress = readingProgress,
        )
    }
}