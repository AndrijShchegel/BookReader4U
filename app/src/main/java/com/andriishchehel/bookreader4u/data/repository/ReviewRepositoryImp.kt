package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.CreateReview
import com.andriishchehel.bookreader4u.data.model.Review
import com.andriishchehel.bookreader4u.data.model.UserReviewUI
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewRepositoryImp(
    private val database: FirebaseFirestore,
    private val baseRepository: BaseRepository
) : ReviewRepository {

    override fun fetchCurrentUserId(): Result<String> {
        return baseRepository.fetchCurrentUserId()
    }

    override suspend fun fetchReviews(bookId: String): Result<List<Review>> {
        val reviews = mutableListOf<Review>()
        val query = database.collection("reviews").whereEqualTo("reviewTo", bookId).get().await()
        for (reviewDoc in query.documents) {
            val timestamp = reviewDoc.getTimestamp("timestamp") ?: Timestamp.now()
            val date = SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(timestamp.toDate())

            val reviewerId = reviewDoc.getString("reviewFrom") ?: continue

            val userDoc = baseRepository.fetchDocument("users", reviewerId).getOrElse {
                return Result.failure(it)
            }
            val reviewerName = userDoc.getString("name") ?: ""
            val reviewerAvatar = userDoc.getString("profileImage") ?: ""

            reviews.add(
                Review(
                    reviewerName = reviewerName,
                    reviewerAvatar = reviewerAvatar,
                    rating = reviewDoc.getDouble("reviewScore")?.toFloat() ?: 0f,
                    text = reviewDoc.getString("reviewText") ?: "",
                    date = date,
                )
            )
        }
        return Result.success(reviews)
    }

    override suspend fun submitUserReview(
        reviewId: String,
        review: CreateReview,
        startingScore: Float
    ): Result<UserReviewUI> {
        var currentReviewId = reviewId
        if (currentReviewId.isNotEmpty()) {
            setReview(currentReviewId, review, startingScore).getOrElse {
                return Result.failure(it)
            }
        } else {
            currentReviewId = createReview(review).getOrElse {
                return Result.failure(it)
            }
        }
        val userDoc = baseRepository.fetchDocument("users", review.reviewFrom).getOrElse {
            return Result.failure(it)
        }
        val username = userDoc.getString("name") ?: ""
        val imageUrl = userDoc.getString("profileImage") ?: ""

        updateAverageRatings(review.reviewTo).getOrElse {
            return Result.failure(it)
        }
        return Result.success(
            UserReviewUI(
                reviewId = currentReviewId,
                username = username,
                profileImage = imageUrl,
                timestamp = review.timestamp,
                rating = review.reviewScore,
                text = review.reviewText
            )
        )
    }

    private suspend fun createReview(review: CreateReview): Result<String> {
        val reviewId = baseRepository.createDocument("reviews", review).getOrElse {
            return Result.failure(it)
        }

        val userId = review.reviewFrom
        val bookId = review.reviewTo

        val starKey = review.reviewScore.toInt().toString()

        val userUpdate = mapOf("reviews" to FieldValue.arrayUnion(reviewId))
        val bookUpdates = mapOf(
            "reviews" to FieldValue.arrayUnion(reviewId),
            "starRatings.$starKey" to FieldValue.increment(1),
            "totalRating" to FieldValue.increment(review.reviewScore.toLong()),
            "reviewCount" to FieldValue.increment(1),
        )

        baseRepository.updateDocument("users", userId, userUpdate).getOrElse {
            return Result.failure(it)
        }
        baseRepository.updateDocument("books", bookId, bookUpdates).getOrElse {
            return Result.failure(it)
        }
        return Result.success(reviewId)
    }

    private suspend fun setReview(
        reviewId: String,
        review: CreateReview,
        startingScore: Float
    ): Result<Unit> {
        val scoreChangedBy = (review.reviewScore - startingScore)

        if (scoreChangedBy != 0f) {
            val bookUpdates = mapOf(
                "starRatings.${review.reviewScore.toInt()}" to FieldValue.increment(1),
                "starRatings.${startingScore.toInt()}" to FieldValue.increment(-1),
                "totalRating" to FieldValue.increment(scoreChangedBy.toLong())
            )
            baseRepository.updateDocument("books", review.reviewTo, bookUpdates).getOrElse {
                return Result.failure(it)
            }
        }

        baseRepository.setDocument("reviews", reviewId, review).getOrElse {
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
}