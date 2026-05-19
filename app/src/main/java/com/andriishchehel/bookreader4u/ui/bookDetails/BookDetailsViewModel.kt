package com.andriishchehel.bookreader4u.ui.bookDetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andriishchehel.bookreader4u.data.model.Book
import com.andriishchehel.bookreader4u.data.model.UserProfile
import com.andriishchehel.bookreader4u.data.model.UserReviewUI
import com.andriishchehel.bookreader4u.data.repository.BookDetailsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val repository: BookDetailsRepository
) : ViewModel() {

    val ratingProgress = MutableLiveData<List<Int>>() // порядок: [5★, 4★, ..., 1★]
    private var isBookSaved = false

    fun computeRatingProgress(book: Book) {
        val reviewCount = book.reviewCount
        if (reviewCount == 0L) {
            ratingProgress.value = listOf(0, 0, 0, 0, 0)
            return
        }

        val result = (5 downTo 1).map { stars ->
            val count = book.starRatings[stars] ?: 0
            val progress = (count * 100) / reviewCount
            when {
                progress == 0L -> 0
                progress <= 4L -> 4
                else -> progress.toInt()
            }
        }

        ratingProgress.value = result
    }

    suspend fun getUserReview(bookId: String): Result<UserReviewUI> {
        return repository.fetchUserReview(bookId)
    }

    suspend fun getUserData(): Result<UserProfile> {
        return repository.getCurrentUserProfile()
    }

    suspend fun getReview(reviewId: String): Result<UserReviewUI> {
        return repository.fetchReview(reviewId)
    }

    suspend fun deleteUserReview(reviewId: String): Result<Unit> {
        return repository.deleteReview(reviewId)
    }

    suspend fun isSaved(bookId: String): Result<Boolean> {
        return repository.isSaved(bookId).map {
            isBookSaved = it
            it
        }
    }

    suspend fun changeState(bookId: String): Result<String> {
        if (isBookSaved) {
            removeFromSaved(bookId).onFailure {
                return Result.failure(it)
            }
            isBookSaved = false
            return Result.success("removed")
        } else {
            addToSaved(bookId).onFailure {
                return Result.failure(it)
            }
            isBookSaved = true
            return Result.success("saved")
        }
    }

    private suspend fun addToSaved(bookId: String): Result<Unit> {
        return repository.addToSaved(bookId)
    }

    private suspend fun removeFromSaved(bookId: String): Result<Unit> {
        return repository.removeFromSaved(bookId)
    }

    suspend fun markAsStarted(bookId: String) {
        repository.markAsReading(bookId)
    }
}
