package com.agrima.campuscart.ui.details

import com.agrima.campuscart.data.model.Product
import com.agrima.campuscart.data.model.User

sealed interface BookingState {
    object Idle : BookingState
    object Loading : BookingState
    object Success : BookingState
    data class Error(val message: String) : BookingState
}

sealed interface ProductDetailsUiState {
    object Loading : ProductDetailsUiState
    data class Success(
        val product: Product,
        val seller: User?,
        val bookingState: BookingState = BookingState.Idle
    ) : ProductDetailsUiState
    data class Error(val message: String) : ProductDetailsUiState
}
