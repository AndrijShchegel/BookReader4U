package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.UserProfile
import com.andriishchehel.bookreader4u.data.model.UserReviewUI

interface BookDetailsRepository {
    suspend fun fetchUserReview(bookId: String): Result<UserReviewUI>
    suspend fun getCurrentUserProfile(): Result<UserProfile>
    suspend fun fetchReview(reviewId: String): Result<UserReviewUI>
    suspend fun deleteReview(reviewId: String): Result<Unit>
    suspend fun isSaved(bookId: String): Result<Boolean>
    suspend fun addToSaved(bookId: String): Result<Unit>
    suspend fun removeFromSaved(bookId: String): Result<Unit>
    suspend fun markAsReading(bookId: String)
}