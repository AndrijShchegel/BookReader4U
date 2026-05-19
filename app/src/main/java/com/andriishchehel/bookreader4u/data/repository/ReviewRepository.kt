package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.CreateReview
import com.andriishchehel.bookreader4u.data.model.Review
import com.andriishchehel.bookreader4u.data.model.UserReviewUI

interface ReviewRepository {
    fun fetchCurrentUserId(): Result<String>
    suspend fun fetchReviews(bookId: String): Result<List<Review>>
    suspend fun submitUserReview(reviewId: String, review: CreateReview, startingScore: Float): Result<UserReviewUI>
}