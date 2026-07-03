package com.agrima.campuscart.data.repository

import com.agrima.campuscart.data.model.User

interface AuthRepository {
    val currentUserId: String?
    fun isUserLoggedIn(): Boolean
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String, phone: String, campus: String?): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getUserProfile(uid: String): Result<User>
    suspend fun updateUserProfile(user: User): Result<Unit>
}
