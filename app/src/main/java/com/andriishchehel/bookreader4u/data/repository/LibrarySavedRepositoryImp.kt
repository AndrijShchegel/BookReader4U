package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.ContinueBook
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator

class LibrarySavedRepositoryImp(
    private val baseRepository: BaseRepository
) : LibrarySavedRepository {

    override fun getSavedBooks(): Flow<List<ContinueBook>> = flow {
        val uid = baseRepository.fetchCurrentUserId().getOrElse {
            emit(emptyList())
            return@flow
        }
        val userDoc = baseRepository.fetchDocument("users", uid).getOrElse {
            emit(emptyList())
            return@flow
        }

        val savedBookIds =
            (userDoc.get("savedBookIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val books = savedBookIds.mapNotNull { id ->
            baseRepository.fetchDocument("books", id).getOrNull()?.let { doc ->
                parseBook(uid, doc)
            }
        }

        emit(books)
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