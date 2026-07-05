package com.agrima.campuscart.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.agrima.campuscart.data.repository.AuthRepository
import com.agrima.campuscart.di.DependencyContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = authRepository.currentUserId
        if (uid == null) {
            _uiState.value = ProfileUiState.Error("User is not authenticated")
            return
        }

        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            authRepository.getUserProfile(uid)
                .onSuccess { user ->
                    _uiState.value = ProfileUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = ProfileUiState.Error(
                        error.message ?: "Failed to retrieve user profile"
                    )
                }
        }
    }

    fun updateProfile(name: String, phone: String, campus: String?) {
        val currentState = _uiState.value
        if (currentState !is ProfileUiState.Success) return

        val currentUser = currentState.user

        if (name.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Name cannot be empty")
            return
        }

        val trimmedPhone = phone.trim()
        if (trimmedPhone.isNotEmpty()) {
            if (!trimmedPhone.all { it.isDigit() }) {
                _uiState.value = currentState.copy(errorMessage = "Phone number must contain only digits")
                return
            }
            if (trimmedPhone.length !in 10..15) {
                _uiState.value = currentState.copy(errorMessage = "Phone number must be between 10 and 15 digits")
                return
            }
        }

        // Start saving
        _uiState.value = currentState.copy(isSaving = true, errorMessage = null, saveSuccess = false)

        viewModelScope.launch {
            val updatedUser = currentUser.copy(
                name = name.trim(),
                phone = trimmedPhone,
                campus = campus?.trim()?.ifBlank { null },
                updatedAt = System.currentTimeMillis()
            )

            authRepository.updateUserProfile(updatedUser)
                .onSuccess {
                    // Fetch refreshed user from repository
                    authRepository.getUserProfile(currentUser.uid)
                        .onSuccess { refreshedUser ->
                            _uiState.value = ProfileUiState.Success(
                                user = refreshedUser,
                                isSaving = false,
                                errorMessage = null,
                                saveSuccess = true
                            )
                        }
                        .onFailure {
                            _uiState.value = ProfileUiState.Success(
                                user = updatedUser,
                                isSaving = false,
                                errorMessage = null,
                                saveSuccess = true
                            )
                        }
                }
                .onFailure { error ->
                    _uiState.value = ProfileUiState.Success(
                        user = currentUser,
                        isSaving = false,
                        errorMessage = error.message ?: "Failed to save profile changes",
                        saveSuccess = false
                    )
                }
        }
    }

    fun clearSaveSuccess() {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            _uiState.value = currentState.copy(saveSuccess = false)
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            _uiState.value = currentState.copy(errorMessage = null)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(
                    authRepository = DependencyContainer.authRepository
                ) as T
            }
        }
    }
}
