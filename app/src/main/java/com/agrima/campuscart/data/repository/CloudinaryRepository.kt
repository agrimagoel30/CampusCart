package com.agrima.campuscart.data.repository

import com.agrima.campuscart.data.network.CloudinaryUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class CloudinaryRepository(
    client: OkHttpClient,
    private val cloudName: String,
    private val uploadPreset: String
) : ImageRepository {

    private val uploader = CloudinaryUploader(client)

    override suspend fun uploadImage(imageBytes: ByteArray): Result<String> = runCatching {
        uploader.uploadImage(imageBytes, cloudName, uploadPreset)
    }

    override suspend fun uploadImages(images: List<ByteArray>): Result<List<String>> = runCatching {
        withContext(Dispatchers.IO) {
            images.map { imageBytes ->
                async { uploader.uploadImage(imageBytes, cloudName, uploadPreset) }
            }.awaitAll()
        }
    }
}
