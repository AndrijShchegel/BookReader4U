package com.andriishchehel.bookreader4u.ui.bookDetails.review

import androidx.lifecycle.ViewModel
import com.andriishchehel.bookreader4u.data.model.CreateReview
import com.andriishchehel.bookreader4u.data.model.UserReviewUI
import com.andriishchehel.bookreader4u.data.repository.ReviewRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReviewSubmitViewModel @Inject constructor(
    private val repository: ReviewRepository
) : ViewModel() {

    suspend fun submitUserReview(
        reviewScore: Float,
        reviewText: String,
        bookId: String,
        existingReviewId: String,
        startingScore: Float
    ): Result<UserReviewUI> {

        val userId = repository.fetchCurrentUserId().getOrElse {
            return Result.failure(it)
        }
        val review = CreateReview(
            reviewFrom = userId,
            reviewTo = bookId,
            reviewScore = reviewScore,
            reviewText = reviewText,
            timestamp = Timestamp.now(),
        )
        return repository.submitUserReview(existingReviewId, review, startingScore)
    }
}