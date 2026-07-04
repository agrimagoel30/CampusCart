package com.agrima.campuscart.ui.dashboard

import com.agrima.campuscart.data.model.Product

data class DashboardStats(
    val totalListings: Int = 0,
    val availableListings: Int = 0,
    val reservedListings: Int = 0,
    val soldListings: Int = 0,
    val totalViews: Int = 0
)

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    object Empty : DashboardUiState
    data class Success(val products: List<Product>, val stats: DashboardStats) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
