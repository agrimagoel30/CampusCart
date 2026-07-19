package com.agrima.campuscart.data.repository

import com.agrima.campuscart.data.model.Category
import com.agrima.campuscart.data.model.Condition
import com.agrima.campuscart.data.model.Product
import com.agrima.campuscart.data.model.ProductStatus

interface ProductRepository {
    suspend fun getProducts(category: Category? = null): Result<List<Product>>
    suspend fun getProductById(productId: String): Result<Product>
    suspend fun getProductsBySeller(sellerId: String): Result<List<Product>>
    suspend fun createProduct(
        title: String,
        description: String,
        price: Double,
        category: Category,
        condition: Condition,
        imageUrls: List<String>,
        sellerId: String,
        sellerName: String,
        negotiable: Boolean,
        location: String
    ): Result<String>
    suspend fun updateProductStatus(productId: String, status: ProductStatus): Result<Unit>
    suspend fun reserveProduct(productId: String, buyerId: String): Result<Unit>
    suspend fun deleteProduct(productId: String): Result<Unit> // soft delete
    suspend fun incrementProductViews(productId: String): Result<Unit>
    
    // Favorites
    suspend fun addToFavorites(productId: String): Result<Unit>
    suspend fun removeFromFavorites(productId: String): Result<Unit>
    suspend fun getFavoriteProducts(): Result<List<Product>>
    suspend fun isFavorite(productId: String): Result<Boolean>
}
