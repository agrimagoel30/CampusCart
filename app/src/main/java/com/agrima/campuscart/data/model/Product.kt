package com.agrima.campuscart.data.model

import com.google.firebase.firestore.PropertyName

data class Product(
    val id: String = "", // Maps to Firestore Document ID
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: Category = Category.OTHERS,
    val condition: Condition = Condition.GOOD,
    val status: ProductStatus = ProductStatus.AVAILABLE,
    val imageUrls: List<String> = emptyList(),
    val sellerId: String = "",
    val sellerName: String = "",
    val negotiable: Boolean = false,
    val location: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    
    @get:PropertyName("isSoftDeleted")
    val isSoftDeleted: Boolean = false,
    
    val viewsCount: Int = 0,
    
    @get:PropertyName("isReported")
    val isReported: Boolean = false
)
