package com.agrima.campuscart.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.agrima.campuscart.data.model.Product
import com.agrima.campuscart.data.repository.ProductRepository
import com.agrima.campuscart.di.DependencyContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        _uiState.value = FavoritesUiState.Loading
        viewModelScope.launch {
            productRepository.getFavoriteProducts()
                .onSuccess { products ->
                    if (products.isEmpty()) {
                        _uiState.value = FavoritesUiState.Empty
                    } else {
                        _uiState.value = FavoritesUiState.Success(products)
                    }
                }
                .onFailure { exception ->
                    _uiState.value = FavoritesUiState.Error(exception.message ?: "Failed to load favorites")
                }
        }
    }

    fun refreshFavorites() {
        loadFavorites()
    }

    fun removeFavorite(productId: String) {
        viewModelScope.launch {
            // Optimistically update UI if state is Success
            val currentState = _uiState.value
            if (currentState is FavoritesUiState.Success) {
                val updatedList = currentState.products.filter { it.id != productId }
                if (updatedList.isEmpty()) {
                    _uiState.value = FavoritesUiState.Empty
                } else {
                    _uiState.value = FavoritesUiState.Success(updatedList)
                }
            }

            productRepository.removeFromFavorites(productId)
                .onFailure {
                    // On failure, reload list to ensure UI is in sync
                    loadFavorites()
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FavoritesViewModel(
                    productRepository = DependencyContainer.productRepository
                ) as T
            }
        }
    }
}
