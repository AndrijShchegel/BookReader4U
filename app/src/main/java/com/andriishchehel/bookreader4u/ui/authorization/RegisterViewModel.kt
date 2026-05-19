package com.andriishchehel.bookreader4u.ui.authorization

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.andriishchehel.bookreader4u.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    fun isValidInput(username: String, email: String, password: String, confirmPassword: String): String? {
        if (password != confirmPassword) return "passwords not match"
        if (!validatePassword(password).isNullOrEmpty() && !validateEmail(email).isNullOrEmpty() && !validateUsername(username).isNullOrEmpty()) {
            return "check inputs"
        }
        return null
    }

    fun validatePassword(password: String): String? {
        return if (password.length < 6) {
            return "password < 6"
        } else null
    }

    fun validateUsername(username: String): String? {
        return if (username.length < 3) {
            return "username < 3"
        } else null
    }

    fun validateEmail(email: String): String? {
        return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "email has wrong pattern"
        } else null
    }

    suspend fun register(username: String, email: String, password: String): Result<Unit> {
        return repository.register(username, email, password)
    }
}