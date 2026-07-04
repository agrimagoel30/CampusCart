package com.agrima.campuscart.ui.home

import com.agrima.campuscart.data.model.Product

sealed interface HomeUiState {
    object Loading : HomeUiState
    object Empty : HomeUiState
    data class Success(val products: List<Product>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
