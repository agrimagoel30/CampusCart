package com.agrima.campuscart.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String? = null,
    val campus: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
