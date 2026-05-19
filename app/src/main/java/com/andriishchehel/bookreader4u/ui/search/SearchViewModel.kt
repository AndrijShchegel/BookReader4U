package com.andriishchehel.bookreader4u.ui.search

import androidx.lifecycle.ViewModel
import com.andriishchehel.bookreader4u.data.model.Book
import com.andriishchehel.bookreader4u.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository
) : ViewModel() {

    suspend fun search(
        orderBy: String,
        descendingOrder: Boolean,
        search: String,
        minRating: Float,
        useAndLogic: Boolean,
        included: List<String>,
        excluded: List<String>
    ): List<Book> {
        return repository.search(
            orderBy,
            descendingOrder,
            search,
            minRating,
            useAndLogic,
            included,
            excluded
        )
    }
}