package com.andriishchehel.bookreader4u.ui.bookDetails.review

import androidx.lifecycle.ViewModel
import com.andriishchehel.bookreader4u.data.model.Review
import com.andriishchehel.bookreader4u.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReviewListViewModel @Inject constructor(
    private val repository: ReviewRepository
) : ViewModel() {


    suspend fun getReviews(bookId: String): Result<List<Review>> {
        return repository.fetchReviews(bookId)
    }
}