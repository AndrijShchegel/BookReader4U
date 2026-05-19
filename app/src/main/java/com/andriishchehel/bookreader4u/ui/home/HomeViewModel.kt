package com.andriishchehel.bookreader4u.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andriishchehel.bookreader4u.BookReader4UApplication
import com.andriishchehel.bookreader4u.data.model.Book
import com.andriishchehel.bookreader4u.data.repository.AuthRepository
import com.andriishchehel.bookreader4u.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
): ViewModel() {

    fun getLatestBooks(): Flow<List<Book>> {
        return repository.getLatestBooks()
    }
    fun getMysteryBooks(): Flow<List<Book>> {
        return repository.getMysteryBooks()
    }
}