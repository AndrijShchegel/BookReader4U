package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.Book
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeRepositoryImp(
    private val database: FirebaseFirestore,
): HomeRepository {
    override fun getLatestBooks(): Flow<List<Book>> {
        return database.collection("books")
            .orderBy("createAt", Query.Direction.DESCENDING)
            .limit(20).snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { doc -> parseBook(doc) } }
    }

    override fun getMysteryBooks(): Flow<List<Book>> {
        return database.collection("books")
            .whereArrayContains("genres", "mystery")
            .limit(20).snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { doc -> parseBook(doc) } }
    }

    private fun parseBook(doc: DocumentSnapshot): Book {
        val reviews =
            (doc.get("reviews") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val starRatings = (doc.get("starRatings") as? Map<*, *>)?.mapNotNull {
            val star = (it.key as? String)?.toIntOrNull()
            val count = (it.value as? Number)?.toInt()
            if (star != null && count != null) star to count else null
        }?.toMap() ?: emptyMap()

        return Book(
            bookId = doc.id,
            image = doc.getString("image") ?: "",
            title = doc.getString("title") ?: "",
            author = doc.getString("author") ?: "",
            rating = doc.getDouble("rating")?.toFloat() ?: 0f,
            description = doc.getString("description") ?: "",
            genres = (doc.get("genres") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            reviews = reviews,
            reviewCount = doc.getLong("reviewCount") ?: 0L,
            fileUrl = doc.getString("fileUrl") ?: "",
            starRatings = starRatings
        )
    }
}