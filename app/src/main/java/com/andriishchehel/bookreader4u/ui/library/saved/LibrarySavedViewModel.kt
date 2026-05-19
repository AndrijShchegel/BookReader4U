package com.andriishchehel.bookreader4u.ui.library.saved

import androidx.lifecycle.ViewModel
import com.andriishchehel.bookreader4u.data.model.ContinueBook
import com.andriishchehel.bookreader4u.data.repository.LibrarySavedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class LibrarySavedViewModel @Inject constructor(
    private val repository: LibrarySavedRepository
) : ViewModel() {

    fun fetchSavedBooks(): Flow<List<ContinueBook>> {
        return repository.getSavedBooks()
    }
}