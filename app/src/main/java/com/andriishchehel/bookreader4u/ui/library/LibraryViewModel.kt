package com.andriishchehel.bookreader4u.ui.library

import androidx.lifecycle.ViewModel
import com.andriishchehel.bookreader4u.data.model.ContinueBook
import com.andriishchehel.bookreader4u.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    fun fetchSavedBookSize(): Flow<Int> {
        return repository.fetchSavedBookSize()
    }

    fun fetchShelfsSize(): Flow<Int> {
        return repository.fetchShelfsSize()
    }

    fun fetchReading(): Flow<List<ContinueBook>> {
        return repository.fetchReading()
    }

}