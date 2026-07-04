package com.agrima.campuscart.ui.sell

sealed interface SellUiState {
    object Idle : SellUiState
    object UploadingImages : SellUiState
    object SavingProduct : SellUiState
    object Success : SellUiState
    data class Error(val message: String) : SellUiState
}
