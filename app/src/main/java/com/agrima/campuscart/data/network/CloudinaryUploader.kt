package com.agrima.campuscart.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class CloudinaryUploader(private val client: OkHttpClient) {

    suspend fun uploadImage(
        imageBytes: ByteArray,
        cloudName: String,
        uploadPreset: String
    ): String = withContext(Dispatchers.IO) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", uploadPreset)
            .addFormDataPart(
                "file",
                "image.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorMsg = response.body?.string() ?: response.message
                throw IOException("Cloudinary upload failed: ${response.code} - $errorMsg")
            }
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            val json = JSONObject(responseBody)
            json.getString("secure_url")
        }
    }
}
