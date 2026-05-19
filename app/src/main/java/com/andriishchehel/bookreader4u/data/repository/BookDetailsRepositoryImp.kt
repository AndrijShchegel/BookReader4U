package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.UserProfile
import com.andriishchehel.bookreader4u.data.model.UserReviewUI
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

class BookDetailsRepositoryImp(
    private val baseRepository: BaseRepository
) : BookDetailsRepository {

    override suspend fun fetchUserReview(bookId: String): Result<UserReviewUI> {
        val userId = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        val userResult = baseRepository.fetchDocument("users", userId)
        val userDoc = userResult.getOrElse {
            return Result.failure(it)
        }
        val reviewIds = (userDoc.get("reviews") as? List<*>)?.filterIsInstance<String>()
        if (reviewIds.isNullOrEmpty()) {
            return Result.failure(NoSuchElementException("No reviews found for user"))
        }

        for (reviewId in reviewIds) {
            val reviewResult = baseRepository.fetchDocument("reviews", reviewId)
            val reviewDoc = reviewResult.getOrElse {
                return Result.failure(it)
            }
            val currentBookId = reviewDoc.getString("reviewTo")
            if (currentBookId == bookId) {
                val username = userDoc.getString("name") ?: ""
                val imageUrl = userDoc.getString("profileImage") ?: ""
                val timestamp = reviewDoc.getTimestamp("timestamp") ?: Timestamp.now()
                val rating = reviewDoc.getDouble("reviewScore")?.toFloat() ?: 0f
                val text = reviewDoc.getString("reviewText") ?: ""
                return Result.success(
                    UserReviewUI(
                        username = username,
                        profileImage = imageUrl,
                        reviewId = reviewId,
                        timestamp = timestamp,
                        rating = rating,
                        text = text
                    )
                )
            }
        }
        return Result.failure(NoSuchElementException("No reviews found for user"))
    }

    override suspend fun getCurrentUserProfile(): Result<UserProfile> {
        val userId = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }

        val userDoc = baseRepository.fetchDocument("users", userId).getOrElse {
            return Result.failure(it)
        }

        val name = userDoc.getString("name") ?: ""
        val image = userDoc.getString("profileImage") ?: ""
        return Result.success(UserProfile(name, image))
    }

    override suspend fun fetchReview(reviewId: String): Result<UserReviewUI> {
        val result = baseRepository.fetchDocument("reviews", reviewId)
        val reviewDoc = result.getOrElse {
            return Result.failure(it)
        }
        val reviewerId = reviewDoc.getString("reviewFrom") ?: ""
        val timestamp = reviewDoc.getTimestamp("timestamp") ?: Timestamp.now()
        val rating = reviewDoc.getDouble("reviewScore")?.toFloat() ?: 0f
        val reviewText = reviewDoc.getString("reviewText") ?: ""
        val userResult = baseRepository.fetchDocument("users", reviewerId)
        val userDoc = userResult.getOrElse {
            return Result.failure(it)
        }
        val username = userDoc.getString("name") ?: ""
        val profileImage = userDoc.getString("profileImage") ?: ""

        return Result.success(
            UserReviewUI(
                reviewId = reviewId,
                username = username,
                profileImage = profileImage,
                timestamp = timestamp,
                rating = rating,
                text = reviewText
            )
        )
    }

    override suspend fun deleteReview(reviewId: String): Result<Unit> {
        val result = baseRepository.fetchDocument("reviews", reviewId)
        val reviewDoc = result.getOrElse {
            return Result.failure(it)
        }
        val userId = reviewDoc.getString("reviewFrom") ?: ""
        val bookId = reviewDoc.getString("reviewTo") ?: ""
        val reviewScore = reviewDoc.getLong("reviewScore") ?: 0L

        val userUpdate = mapOf("reviews" to FieldValue.arrayRemove(reviewId))
        val bookUpdates = mapOf(
            "reviews" to FieldValue.arrayRemove(reviewId),
            "reviewCount" to FieldValue.increment(-1),
            "totalRating" to FieldValue.increment(-reviewScore.toDouble()),
            "starRatings.${reviewScore.toInt()}" to FieldValue.increment(-1)
        )

        baseRepository.deleteDocument("reviews", reviewId).getOrElse {
            return Result.failure(it)
        }
        baseRepository.updateDocument("users", userId, userUpdate).getOrElse {
            return Result.failure(it)
        }
        baseRepository.updateDocument("books", bookId, bookUpdates).getOrElse {
            return Result.failure(it)
        }
        updateAverageRatings(bookId).getOrElse {
            return Result.failure(it)
        }
        return Result.success(Unit)
    }

    private suspend fun updateAverageRatings(bookId: String): Result<Unit> {
        val bookDoc = baseRepository.fetchDocument("books", bookId).getOrElse {
            return Result.failure(it)
        }
        val reviewCount = bookDoc.getLong("reviewCount") ?: 0L
        val totalRating = bookDoc.getLong("totalRating") ?: 0L
        if (reviewCount != 0L) {
            val bookUpdate = mapOf("rating" to (totalRating.toFloat() / reviewCount))
            baseRepository.updateDocument("books", bookId, bookUpdate).getOrElse {
                return Result.failure(it)
            }
        }
        return Result.success(Unit)
    }

    override suspend fun isSaved(bookId: String): Result<Boolean> {
        val uid = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        val userDoc = baseRepository.fetchDocument("users", uid).getOrElse {
            return Result.failure(it)
        }
        val savedList = (userDoc.get("savedBookIds") as? List<*>) ?: emptyList<Any>()
        val savedIds = savedList.filterIsInstance<String>()
        return Result.success(bookId in savedIds)
    }

    override suspend fun addToSaved(bookId: String): Result<Unit> {
        val uid = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        val userUpdate = mapOf("savedBookIds" to FieldValue.arrayUnion(bookId))
        return baseRepository.updateDocument("users", uid, userUpdate)
    }

    override suspend fun removeFromSaved(bookId: String): Result<Unit> {
        val uid = baseRepository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        val userUpdate = mapOf("savedBookIds" to FieldValue.arrayRemove(bookId))
        return baseRepository.updateDocument("users", uid, userUpdate)
    }

    override suspend fun markAsReading(bookId: String) {
        val uid = baseRepository.fetchCurrentUserId().getOrElse { return }
        val updates = mapOf("readingIds" to FieldValue.arrayUnion(bookId))
        baseRepository.updateDocument("users", uid, updates)
    }
}