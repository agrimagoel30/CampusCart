package com.agrima.campuscart.ui.details

import com.agrima.campuscart.data.model.Product
import com.agrima.campuscart.data.model.User

sealed interface ProductDetailsUiState {
    object Loading : ProductDetailsUiState
    data class Success(val product: Product, val seller: User?) : ProductDetailsUiState
    data class Error(val message: String) : ProductDetailsUiState
}
