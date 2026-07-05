package com.agrima.campuscart.ui.profile

import com.agrima.campuscart.data.model.User

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(
        val user: User,
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
        val saveSuccess: Boolean = false
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
