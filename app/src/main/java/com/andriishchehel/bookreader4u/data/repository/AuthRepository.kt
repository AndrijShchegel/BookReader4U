package com.andriishchehel.bookreader4u.data.repository

interface AuthRepository {
    fun fetchCurrentUserId(): Result<String>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun currentUserRole(userId: String): Result<String>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
}