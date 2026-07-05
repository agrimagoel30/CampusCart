package com.agrima.campuscart.di

import com.agrima.campuscart.data.repository.AuthRepository
import com.agrima.campuscart.data.repository.AuthRepositoryImpl
import com.agrima.campuscart.data.repository.CloudinaryRepository
import com.agrima.campuscart.data.repository.ImageRepository
import com.agrima.campuscart.data.repository.ProductRepository
import com.agrima.campuscart.data.repository.ProductRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient

object DependencyContainer {
    
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val okHttpClient: OkHttpClient by lazy { OkHttpClient() }
    
    // Cloudinary Configuration
    private const val CLOUDINARY_CLOUD_NAME = "dm9fccx4x"
    private const val CLOUDINARY_UPLOAD_PRESET = "CampusCart_upload"

    val imageRepository: ImageRepository by lazy {
        CloudinaryRepository(okHttpClient, CLOUDINARY_CLOUD_NAME, CLOUDINARY_UPLOAD_PRESET)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(auth, firestore)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(firestore, auth)
    }
}
