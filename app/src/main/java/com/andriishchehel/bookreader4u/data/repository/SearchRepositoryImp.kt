package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.Book
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class SearchRepositoryImp(
    private val database: FirebaseFirestore,
) : SearchRepository {

    override suspend fun search(
        orderBy: String,
        descendingOrder: Boolean,
        search: String,
        minRating: Float,
        useAndLogic: Boolean,
        included: List<String>,
        excluded: List<String>,
    ): List<Book> {
        val snapshot = createQuerySnapshot(included, minRating, orderBy, descendingOrder)
        return processSearchResults(
            snapshot,
            search = search,
            excluded = excluded,
            included = included,
            useAndLogic = useAndLogic
        )
    }

    private suspend fun createQuerySnapshot(
        included: List<String>,
        minRating: Float,
        orderBy: String,
        descendingOrder: Boolean
    ): QuerySnapshot {
        var query: Query = database.collection("books")
        if (included.isNotEmpty()) {
            query = query.whereArrayContainsAny("genres", included)
        }
        if (minRating >= 1) {
            query = query.whereGreaterThanOrEqualTo("rating", minRating)
        }
        return query.orderBy(
            orderBy,
            if (descendingOrder) Query.Direction.DESCENDING else Query.Direction.ASCENDING
        ).get().await()
    }

    private fun processSearchResults(
        snapshot: QuerySnapshot,
        search: String,
        excluded: List<String>,
        included: List<String>,
        useAndLogic: Boolean
    ): List<Book> {
        val bookList = mutableListOf<Book>()
        for (doc in snapshot) {
            val title = doc.getString("title") ?: ""
            val author = doc.getString("author") ?: ""
            if (!title.contains(search, true) && !author.contains(search, true)) continue

            val genres = doc.get("genres") as? List<*> ?: emptyList<Any>()
            val genreStrings = genres.filterIsInstance<String>()
            val excludeOK = excluded.none { genreStrings.contains(it) }

            if (useAndLogic) {
                val includeOK = included.all { genreStrings.contains(it) }
                if (includeOK && excludeOK) bookList.add(getBookInfo(doc))
            } else {
                if (excludeOK) bookList.add(getBookInfo(doc))
            }
        }
        return bookList
    }

    private fun getBookInfo(doc: DocumentSnapshot): Book {
        val reviews = (doc.get("reviews") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val starRatings = when (val data = doc.get("starRatings")) {
            is Map<*, *> -> data.mapNotNull { (key, value) ->
                val star = (key as? String)?.toIntOrNull()
                val count = (value as? Number)?.toInt()
                if (star != null && count != null) star to count else null
            }.toMap()

            else -> emptyMap()
        }


        return Book(
            bookId = doc.id,
            image = doc.getString("image") ?: "",
            title = doc.getString("title") ?: "",
            author = doc.getString("author") ?: "",
            rating = doc.getDouble("rating")?.toFloat() ?: 0f,
            description = doc.getString("description") ?: "",
            genres = (doc.get("genres") as? List<*>)?.filterIsInstance<String>()
                ?: emptyList(),
            reviews = reviews,
            reviewCount = doc.getLong("reviewCount") ?: 0L,
            fileUrl = doc.getString("fileUrl") ?: "",
            starRatings = starRatings,
        )
    }
}