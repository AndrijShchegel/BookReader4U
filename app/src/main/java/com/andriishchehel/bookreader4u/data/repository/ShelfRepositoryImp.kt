package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.ContinueBook
import com.andriishchehel.bookreader4u.data.model.Shelfs
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator

class ShelfRepositoryImp(
    private val database: FirebaseFirestore,
    private val baseRepository: BaseRepository,
) : ShelfRepository {
    override fun fetchShelfs(): Flow<List<Shelfs>> {
        val uid = baseRepository.fetchCurrentUserId().getOrElse {
            return flowOf(emptyList())
        }
        return database.collection("users").document(uid).collection("shelfs").snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { doc -> parseShelf(doc) } }
    }


    private suspend fun parseShelf(doc: DocumentSnapshot): Shelfs {
        val bookList = doc.get("bookIds") as? List<*> ?: emptyList<String>()
        val bookIds = bookList.filterIsInstance<String>()

        val firstBookImage = if (bookIds.isNotEmpty()) {
            val firstBookDoc = baseRepository.fetchDocument("books", bookIds[0]).getOrNull()
            firstBookDoc?.getString("image") ?: ""
        } else {
            ""
        }
        return Shelfs(
            shelfId = doc.id,
            shelfName = doc.getString("name") ?: "",
            bookIds = bookIds,
            firstBookImage = firstBookImage
        )
    }

    override suspend fun createShelf(name: String): Result<Unit> = try {
        val userId = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        val data = mapOf("name" to name)
        database.collection("users").document(userId).collection("shelfs").add(data).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateShelfName(shelfId: String, name: String): Result<Unit> = try {
        val userId = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        database.collection("users").document(userId).collection("shelfs").document(shelfId)
            .update("name", name).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }


    override suspend fun deleteShelf(shelfId: String): Result<Unit> = try {
        val userId = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        database.collection("users").document(userId).collection("shelfs").document(shelfId)
            .delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun fetchShelfBooks(shelfId: String): Flow<List<ContinueBook>> {
        return baseRepository.fetchCurrentUserId().getOrElse {
            return flowOf(emptyList())
        }.let { uid ->
            database.collection("users")
                .document(uid)
                .collection("shelfs")
                .document(shelfId)
                .snapshots()
                .flatMapLatest { snapshot ->
                    val bookList = snapshot.get("bookIds") as? List<*> ?: emptyList<Any>()
                    val bookIds = bookList.filterIsInstance<String>()

                    flow {
                        val books = bookIds.mapNotNull { id ->
                            baseRepository.fetchDocument("books", id).getOrNull()?.let { doc ->
                                parseBook(uid, doc)
                            }
                        }
                        emit(books)
                    }
                }
        }
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

    override suspend fun removeBookFromShelf(bookId: String, shelfId: String): Result<Unit> = try {
        val userId = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        val updateBooks = mapOf("bookIds" to FieldValue.arrayRemove(bookId))
        database.collection("users").document(userId).collection("shelfs").document(shelfId)
            .update(updateBooks).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addBooksToShelf(bookIds: List<String>, shelfId: String): Result<Unit> =
        try {
            val userId = baseRepository.fetchCurrentUserId().getOrElse {
                return Result.failure(it)
            }
            val updateBooks = mapOf("bookIds" to FieldValue.arrayUnion(*bookIds.toTypedArray()))
            database.collection("users").document(userId).collection("shelfs").document(shelfId)
                .update(updateBooks).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun fetchAvailableBooks(shelfId: String): Result<List<ContinueBook>> {
        val userId = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        val userDoc = baseRepository.fetchDocument("users", userId).getOrElse {
            return Result.failure(it)
        }
        val shelfDoc =
            database.collection("users").document(userId).collection("shelfs").document(shelfId)
                .get()
                .await()
        val shelfBookList = shelfDoc.get("bookIds") as? List<*> ?: emptyList<Any>()
        val shelfBookIds = shelfBookList.filterIsInstance<String>()
        val readingList = userDoc.get("readingIds") as? List<*> ?: emptyList<Any>()
        val availableBookIds =
            readingList.filterIsInstance<String>().filterNot { it in shelfBookIds }

        val books = mutableListOf<ContinueBook>()
        for (bookId in availableBookIds) {
            val bookDoc = baseRepository.fetchDocument("books", bookId).getOrNull()
            bookDoc?.let {
                val book = ContinueBook(
                    bookId = it.id,
                    title = it.getString("title") ?: "",
                    author = it.getString("author") ?: "",
                    image = it.getString("image") ?: "",
                    fileUrl = it.getString("fileUrl") ?: "",
                    readingProgress = ((it.getDouble("progress") ?: 0.0) * 100).toInt()
                )
                books.add(book)
            }
        }

        return Result.success(books)
    }
}