package com.agrima.campuscart.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.agrima.campuscart.data.model.ProductStatus
import com.agrima.campuscart.data.repository.AuthRepository
import com.agrima.campuscart.data.repository.ProductRepository
import com.agrima.campuscart.di.DependencyContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        _uiState.value = DashboardUiState.Loading
        val sellerId = authRepository.currentUserId
        if (sellerId == null) {
            _uiState.value = DashboardUiState.Error("User is not authenticated")
            return
        }

        viewModelScope.launch {
            productRepository.getProductsBySeller(sellerId)
                .onSuccess { products ->
                    if (products.isEmpty()) {
                        _uiState.value = DashboardUiState.Empty
                    } else {
                        val stats = DashboardStats(
                            totalListings = products.size,
                            availableListings = products.count { it.status == ProductStatus.AVAILABLE },
                            reservedListings = products.count { it.status == ProductStatus.RESERVED },
                            soldListings = products.count { it.status == ProductStatus.SOLD },
                            totalViews = products.sumOf { it.viewsCount }
                        )
                        _uiState.value = DashboardUiState.Success(products, stats)
                    }
                }
                .onFailure { error ->
                    _uiState.value = DashboardUiState.Error(
                        error.message ?: "Failed to load seller listings"
                    )
                }
        }
    }

    fun updateProductStatus(productId: String, newStatus: ProductStatus) {
        viewModelScope.launch {
            productRepository.updateProductStatus(productId, newStatus)
                .onSuccess {
                    loadDashboard() // Auto-refresh immediately on success
                }
                .onFailure { error ->
                    // Even if update status fails, reload dashboard or post error
                    loadDashboard()
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(
                    productRepository = DependencyContainer.productRepository,
                    authRepository = DependencyContainer.authRepository
                ) as T
            }
        }
    }
}
