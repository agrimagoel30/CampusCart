package com.agrima.campuscart.data.repository

interface ImageRepository {
    suspend fun uploadImage(imageBytes: ByteArray): Result<String>
    suspend fun uploadImages(images: List<ByteArray>): Result<List<String>>
}
