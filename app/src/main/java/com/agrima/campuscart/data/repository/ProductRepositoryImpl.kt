package com.agrima.campuscart.data.repository

import com.agrima.campuscart.data.model.Category
import com.agrima.campuscart.data.model.Condition
import com.agrima.campuscart.data.model.Product
import com.agrima.campuscart.data.model.ProductStatus
import com.agrima.campuscart.data.util.await
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class ProductRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ProductRepository {

    private fun DocumentSnapshot.toProduct(): Product? {
        val product = toObject(Product::class.java) ?: return null
        return product.copy(id = this.id)
    }

    override suspend fun getProducts(category: Category?): Result<List<Product>> = withContext(Dispatchers.IO) {
        runCatching {
            var query = firestore.collection("products")
                .whereEqualTo("isSoftDeleted", false)
                .whereEqualTo("status", ProductStatus.AVAILABLE.name)

            if (category != null) {
                query = query.whereEqualTo("category", category.name)
            }

            val snapshot = query.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            snapshot.documents.mapNotNull { it.toProduct() }
        }
    }

    override suspend fun getProductById(productId: String): Result<Product> = withContext(Dispatchers.IO) {
        runCatching {
            val snapshot = firestore.collection("products").document(productId).get().await()
            if (!snapshot.exists()) {
                throw Exception("Product not found")
            }
            snapshot.toProduct() ?: throw Exception("Failed to parse product")
        }
    }

    override suspend fun getProductsBySeller(sellerId: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        runCatching {
            val snapshot = firestore.collection("products")
                .whereEqualTo("sellerId", sellerId)
                .whereEqualTo("isSoftDeleted", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toProduct() }
        }
    }

    override suspend fun createProduct(
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
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val docRef = firestore.collection("products").document()
            val currentTime = System.currentTimeMillis()
            val product = Product(
                id = docRef.id,
                title = title,
                description = description,
                price = price,
                category = category,
                condition = condition,
                status = ProductStatus.AVAILABLE,
                imageUrls = imageUrls,
                sellerId = sellerId,
                sellerName = sellerName,
                negotiable = negotiable,
                location = location,
                createdAt = currentTime,
                updatedAt = currentTime,
                isSoftDeleted = false,
                viewsCount = 0,
                isReported = false
            )
            docRef.set(product).await()
            docRef.id
        }
    }

    override suspend fun updateProductStatus(productId: String, status: ProductStatus): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            firestore.collection("products").document(productId).update(
                "status", status.name,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Unit
        }
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            firestore.collection("products").document(productId).update(
                "isSoftDeleted", true,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Unit
        }
    }

    override suspend fun incrementProductViews(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            firestore.collection("products").document(productId).update(
                "viewsCount", FieldValue.increment(1)
            ).await()
            Unit
        }
    }

    override suspend fun addToFavorites(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUser?.uid ?: throw Exception("User is not logged in")
            val data = mapOf(
                "productId" to productId,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(userId)
                .collection("favorites").document(productId)
                .set(data).await()
            Unit
        }
    }

    override suspend fun removeFromFavorites(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUser?.uid ?: throw Exception("User is not logged in")
            firestore.collection("users").document(userId)
                .collection("favorites").document(productId)
                .delete().await()
            Unit
        }
    }

    override suspend fun isFavorite(productId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUser?.uid ?: throw Exception("User is not logged in")
            val snapshot = firestore.collection("users").document(userId)
                .collection("favorites").document(productId)
                .get().await()
            snapshot.exists()
        }
    }

    override suspend fun getFavoriteProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUser?.uid ?: throw Exception("User is not logged in")
            val favsSnapshot = firestore.collection("users").document(userId)
                .collection("favorites")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            
            val productIds = favsSnapshot.documents.map { it.id }
            if (productIds.isEmpty()) {
                emptyList()
            } else {
                productIds.map { productId ->
                    async { getProductById(productId).getOrNull() }
                }.awaitAll().filterNotNull()
            }
        }
    }
}
