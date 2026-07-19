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

    val currentUserId: String? get() = authRepository.currentUserId

    init {
        loadProductDetails()
    }

    fun reserveProduct() {
        val currentState = _uiState.value
        if (currentState !is ProductDetailsUiState.Success) return

        val buyerId = authRepository.currentUserId
        if (buyerId == null) {
            _uiState.value = currentState.copy(bookingState = BookingState.Error("User is not logged in"))
            return
        }

        // Set booking state to Loading, leaving the rest of the product data unchanged
        _uiState.value = currentState.copy(bookingState = BookingState.Loading)

        viewModelScope.launch {
            productRepository.reserveProduct(productId, buyerId)
                .onSuccess {
                    _uiState.value = currentState.copy(bookingState = BookingState.Success)
                    // Reload product details to update status quietly without flashing loading screen
                    loadProductDetailsAfterBooking()
                }
                .onFailure { error ->
                    _uiState.value = currentState.copy(bookingState = BookingState.Error(error.message ?: "Failed to reserve product"))
                }
        }
    }

    fun clearBookingState() {
        val currentState = _uiState.value
        if (currentState is ProductDetailsUiState.Success) {
            _uiState.value = currentState.copy(bookingState = BookingState.Idle)
        }
    }

    fun loadProductDetails() {
        _uiState.value = ProductDetailsUiState.Loading
        viewModelScope.launch {
            productRepository.getProductById(productId)
                .onSuccess { product ->
                    incrementProductViews()
                    authRepository.getUserProfile(product.sellerId)
                        .onSuccess { seller ->
                            _uiState.value = ProductDetailsUiState.Success(product, seller)
                        }
                        .onFailure {
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

    private fun loadProductDetailsAfterBooking() {
        // Loads details quietly on success without flashing full screen loading
        viewModelScope.launch {
            productRepository.getProductById(productId)
                .onSuccess { product ->
                    authRepository.getUserProfile(product.sellerId)
                        .onSuccess { seller ->
                            val currentState = _uiState.value
                            if (currentState is ProductDetailsUiState.Success) {
                                _uiState.value = ProductDetailsUiState.Success(product, seller, currentState.bookingState)
                            } else {
                                _uiState.value = ProductDetailsUiState.Success(product, seller)
                            }
                        }
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
