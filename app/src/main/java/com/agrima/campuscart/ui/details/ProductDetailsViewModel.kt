package com.agrima.campuscart.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.agrima.campuscart.data.repository.AuthRepository
import com.agrima.campuscart.data.repository.ProductRepository
import com.agrima.campuscart.di.DependencyContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    private val productId: String,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailsUiState>(ProductDetailsUiState.Loading)
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProductDetails()
    }

    fun loadProductDetails() {
        _uiState.value = ProductDetailsUiState.Loading
        viewModelScope.launch {
            productRepository.getProductById(productId)
                .onSuccess { product ->
                    // Increment the product view count only after successful load
                    incrementProductViews()

                    // Fetch seller profile to retrieve phone/contact details
                    authRepository.getUserProfile(product.sellerId)
                        .onSuccess { seller ->
                            _uiState.value = ProductDetailsUiState.Success(product, seller)
                        }
                        .onFailure {
                            // If seller profile fails to load, still show product but without phone contact
                            _uiState.value = ProductDetailsUiState.Success(product, null)
                        }
                }
                .onFailure { error ->
                    _uiState.value = ProductDetailsUiState.Error(
                        error.message ?: "Failed to load product details"
                    )
                }
        }
    }

    private fun incrementProductViews() {
        viewModelScope.launch {
            productRepository.incrementProductViews(productId)
        }
    }

    companion object {
        fun provideFactory(
            productId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProductDetailsViewModel(
                    productId = productId,
                    productRepository = DependencyContainer.productRepository,
                    authRepository = DependencyContainer.authRepository
                ) as T
            }
        }
    }
}
