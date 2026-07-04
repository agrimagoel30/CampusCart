package com.agrima.campuscart.ui.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.agrima.campuscart.data.model.Category
import com.agrima.campuscart.data.model.Condition
import com.agrima.campuscart.data.repository.AuthRepository
import com.agrima.campuscart.data.repository.ImageRepository
import com.agrima.campuscart.data.repository.ProductRepository
import com.agrima.campuscart.di.DependencyContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SellViewModel(
    private val productRepository: ProductRepository,
    private val imageRepository: ImageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SellUiState>(SellUiState.Idle)
    val uiState: StateFlow<SellUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = SellUiState.Idle
    }

    fun createProduct(
        title: String,
        description: String,
        priceString: String,
        category: Category,
        condition: Condition,
        negotiable: Boolean,
        location: String,
        imagesBytes: List<ByteArray>
    ) {
        if (title.isBlank()) {
            _uiState.value = SellUiState.Error("Title cannot be empty")
            return
        }
        if (description.isBlank()) {
            _uiState.value = SellUiState.Error("Description cannot be empty")
            return
        }
        val price = priceString.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            _uiState.value = SellUiState.Error("Price must be a valid number greater than 0")
            return
        }
        if (location.isBlank()) {
            _uiState.value = SellUiState.Error("Location cannot be empty")
            return
        }
        if (imagesBytes.isEmpty()) {
            _uiState.value = SellUiState.Error("Please select at least one image")
            return
        }

        _uiState.value = SellUiState.UploadingImages

        viewModelScope.launch {
            try {
                val sellerId = authRepository.currentUserId 
                    ?: throw Exception("User is not logged in")

                val sellerProfile = authRepository.getUserProfile(sellerId).getOrThrow()
                val sellerName = sellerProfile.name

                val uploadResult = imageRepository.uploadImages(imagesBytes)
                
                uploadResult.onSuccess { urls ->
                    _uiState.value = SellUiState.SavingProduct
                    
                    productRepository.createProduct(
                        title = title,
                        description = description,
                        price = price,
                        category = category,
                        condition = condition,
                        imageUrls = urls,
                        sellerId = sellerId,
                        sellerName = sellerName,
                        negotiable = negotiable,
                        location = location
                    ).onSuccess {
                        _uiState.value = SellUiState.Success
                    }.onFailure { error ->
                        _uiState.value = SellUiState.Error(error.message ?: "Failed to save product listing")
                    }
                }.onFailure { error ->
                    _uiState.value = SellUiState.Error(error.message ?: "Failed to upload images")
                }
            } catch (e: Exception) {
                _uiState.value = SellUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SellViewModel(
                    productRepository = DependencyContainer.productRepository,
                    imageRepository = DependencyContainer.imageRepository,
                    authRepository = DependencyContainer.authRepository
                ) as T
            }
        }
    }
}
