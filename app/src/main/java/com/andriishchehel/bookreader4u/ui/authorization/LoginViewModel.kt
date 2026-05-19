package com.andriishchehel.bookreader4u.ui.authorization

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.andriishchehel.bookreader4u.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    fun fetchCurrentUserId(): Result<String> {
        return repository.fetchCurrentUserId()
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return if (isValidInput(email, password)) {
            repository.login(email, password)
        } else {
            Result.failure(IllegalArgumentException("Not valid input"))
        }
    }

    suspend fun checkUserStatus(): Result<String> {
        val userId = fetchCurrentUserId().getOrElse { return Result.failure(it) }
        return repository.currentUserRole(userId)
    }

    private fun isValidInput(email: String, password: String): Boolean {
        return (validatePassword(password).isNullOrEmpty() && validateEmail(email).isNullOrEmpty())
    }

    fun validatePassword(password: String): String? {
        return if (password.length < 6) {
            "password < 6"
        } else null
    }

    fun validateEmail(email: String): String? {
        return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "email has wrong pattern"
        } else null
    }
}