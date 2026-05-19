package com.andriishchehel.bookreader4u.data.repository

import com.andriishchehel.bookreader4u.data.model.CreateUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepositoryImp(
    private val auth: FirebaseAuth,
    private val baseRepository: BaseRepository
) : AuthRepository {

    override fun fetchCurrentUserId(): Result<String> {
        return baseRepository.fetchCurrentUserId()
    }

    override suspend fun login(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun currentUserRole(userId: String): Result<String> {
        val resultDoc = baseRepository.fetchDocument("users", userId).getOrElse {
            return Result.failure(it)
        }
        val role = resultDoc.getString("role") ?: return Result.failure(IllegalStateException("Role field is missing in database"))
        return Result.success(role)
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        val userId = createUser(email, password).getOrElse {
            return Result.failure(it)
        }

        val user = CreateUser(name = name, email = email)
        return baseRepository.setDocument(
            collectionId = "users",
            documentId = userId,
            data = user,
        )
    }

    private suspend fun createUser(email: String, password: String): Result<String> = try {
        val user = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = user.user?.uid
        if (uid != null) {
            Result.success(uid)
        } else {
            Result.failure(IllegalStateException("UID is null"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
