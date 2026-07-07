package com.turkcell.rencarapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.auth.AuthRepository
import com.turkcell.rencarapp.data.auth.UserRole
import com.turkcell.rencarapp.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileEffect> = _effect.receiveAsFlow()

    init {
        // İlk açılışta mevcut temayı durumumuza eşitliyoruz
        _uiState.update { it.copy(isDarkMode = ThemeManager.isDarkMode.value ?: false) }
        onIntent(ProfileIntent.LoadProfile)
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> fetchUserProfile()
            is ProfileIntent.LogoutClicked -> logout()
            is ProfileIntent.MenuItemClicked -> handleMenuItemClick(intent.item)
            is ProfileIntent.ThemeToggleClicked -> toggleTheme()
            is ProfileIntent.EditProfileClicked -> handleEditProfile()
        }
    }

    private fun fetchUserProfile() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.getCurrentUser()

            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fullName = user.fullName,
                        phone = user.phone ?: "",
                        isLicenseVerified = user.role == UserRole.CUSTOMER
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _effect.send(ProfileEffect.ShowError(error.message ?: "Kullanıcı bilgileri alınamadı."))
            }
        }
    }

    private fun logout() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.logout()
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                _effect.send(ProfileEffect.NavigateToSplash)
            }.onFailure { error ->
                _effect.send(ProfileEffect.ShowError(error.message ?: "Çıkış yapılamadı."))
            }
        }
    }

    private fun handleMenuItemClick(item: String) {
        viewModelScope.launch {
            if (item == "Ödeme yöntemleri") {
                _effect.send(ProfileEffect.NavigateToWallet) // Cüzdan sayfasına yönlendirir
            } else {
                _effect.send(ProfileEffect.ShowError("$item ekranı henüz aktif değil."))
            }
        }
    }

    private fun handleEditProfile() {
        viewModelScope.launch {
            _effect.send(ProfileEffect.ShowError("Profil düzenleme ekranı yakında aktif olacak."))
        }
    }

    private fun toggleTheme() {
        val newState = !_uiState.value.isDarkMode
        _uiState.update { it.copy(isDarkMode = newState) }

        // YENİ: Global temayı değiştiriyoruz (Uygulamanın anında tepki vermesini sağlar)
        ThemeManager.isDarkMode.value = newState
    }
}