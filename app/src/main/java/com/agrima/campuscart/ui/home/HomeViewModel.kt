package com.agrima.campuscart.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.agrima.campuscart.data.model.Product
import com.agrima.campuscart.data.repository.AuthRepository
import com.agrima.campuscart.data.repository.ProductRepository
import com.agrima.campuscart.di.DependencyContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _rawProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow(UiCategory.ALL)
    val userName = MutableStateFlow("User")
    
    val currentUserId: String? get() = authRepository.currentUserId

    val uiState: StateFlow<HomeUiState> = combine(
        _rawProducts,
        _isLoading,
        _errorMessage,
        searchQuery,
        selectedCategory
    ) { rawList, loading, error, query, category ->
        if (loading) {
            HomeUiState.Loading
        } else if (error != null) {
            HomeUiState.Error(error)
        } else {
            val filtered = rawList.filter { product ->
                val matchesQuery = query.isBlank() || product.title.contains(query, ignoreCase = true)
                val matchesCategory = category == UiCategory.ALL || product.category == category.domainCategory
                matchesQuery && matchesCategory
            }
            if (filtered.isEmpty()) {
                HomeUiState.Empty
            } else {
                HomeUiState.Success(filtered)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    init {
        loadProducts()
        loadFavorites()
        loadCurrentUserName()
    }

    fun loadProducts() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            productRepository.getProducts(null)
                .onSuccess { list ->
                    _rawProducts.value = list
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load products"
                    _isLoading.value = false
                }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            productRepository.getFavoriteProducts()
                .onSuccess { list ->
                    _favoriteIds.value = list.map { it.id }.toSet()
                }
        }
    }

    fun toggleFavorite(productId: String) {
        val currentlyFavorite = _favoriteIds.value.contains(productId)
        viewModelScope.launch {
            if (currentlyFavorite) {
                _favoriteIds.value = _favoriteIds.value - productId
                productRepository.removeFromFavorites(productId)
                    .onFailure {
                        _favoriteIds.value = _favoriteIds.value + productId
                    }
            } else {
                _favoriteIds.value = _favoriteIds.value + productId
                productRepository.addToFavorites(productId)
                    .onFailure {
                        _favoriteIds.value = _favoriteIds.value - productId
                    }
            }
        }
    }

    private fun loadCurrentUserName() {
        val currentUid = authRepository.currentUserId
        if (currentUid != null) {
            viewModelScope.launch {
                authRepository.getUserProfile(currentUid).onSuccess { user ->
                    userName.value = user.name
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSelectedCategory(category: UiCategory) {
        selectedCategory.value = category
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    productRepository = DependencyContainer.productRepository,
                    authRepository = DependencyContainer.authRepository
                ) as T
            }
        }
    }
}
