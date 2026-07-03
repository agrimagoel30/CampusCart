package com.agrima.campuscart.data.repository

import com.agrima.campuscart.data.model.User
import com.agrima.campuscart.data.util.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User ID not found after login")
            getUserProfile(uid).getOrThrow()
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        campus: String?
    ): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User ID not found after registration")
            
            val currentTime = System.currentTimeMillis()
            val newUser = User(
                uid = uid,
                name = name,
                email = email,
                phone = phone,
                avatarUrl = null,
                campus = campus,
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            firestore.collection("users").document(uid).set(newUser).await()
            newUser
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signOut()
        }
    }

    override suspend fun getUserProfile(uid: String): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val snapshot = firestore.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java) ?: throw Exception("User profile not found in database")
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
            firestore.collection("users").document(user.uid).set(updatedUser).await()
            Unit
        }
    }
}
