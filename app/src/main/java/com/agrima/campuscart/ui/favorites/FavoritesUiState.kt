package com.agrima.campuscart.ui.favorites

import com.agrima.campuscart.data.model.Product

sealed interface FavoritesUiState {
    object Loading : FavoritesUiState
    object Empty : FavoritesUiState
    data class Success(val products: List<Product>) : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
}
