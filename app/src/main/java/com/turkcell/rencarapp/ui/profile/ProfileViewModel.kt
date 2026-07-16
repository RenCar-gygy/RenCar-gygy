package com.turkcell.rencarapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.auth.AuthRepository
import com.turkcell.rencarapp.data.auth.UserRole
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rentalRepository: RentalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<ProfileEffect> = _effect.asSharedFlow()

    init {
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
            is ProfileIntent.CopyReferralCodeClicked -> copyReferralCode()
        }
    }

    private fun copyReferralCode() {
        val code = _uiState.value.referralCode.trim()
        if (code.isBlank()) {
            viewModelScope.launch {
                _effect.emit(ProfileEffect.ShowError("Davet kodunuz henüz oluşturulmamış."))
            }
            return
        }
        viewModelScope.launch {
            _effect.emit(ProfileEffect.CopyReferralCode(code))
        }
    }

    private fun fetchUserProfile() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userResult = authRepository.getCurrentUser()
            val statsResult = rentalRepository.getStats()

            userResult.onSuccess { user ->
                val stats = statsResult.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fullName = user.fullName,
                        phone = user.phone ?: "",
                        referralCode = user.referralCode.orEmpty(),
                        isLicenseVerified = user.role == UserRole.CUSTOMER,
                        monthlyTripCount = stats?.tripCount ?: 0,
                        monthlyTotalSpent = stats?.totalSpent?.let { spent ->
                            String.format(java.util.Locale.forLanguageTag("tr-TR"), "₺%.0f", spent)
                        } ?: "",
                        monthlyTotalKm = stats?.totalKm?.let { km ->
                            String.format(java.util.Locale.forLanguageTag("tr-TR"), "%.1f km", km)
                        } ?: "",
                        monthlyTotalMinutes = stats?.totalMinutes?.let { minutes ->
                            formatDurationMinutes(minutes)
                        } ?: "",
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _effect.emit(ProfileEffect.ShowError(error.message ?: "Kullanıcı bilgileri alınamadı."))
            }
        }
    }

    private fun logout() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.logout()
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                _effect.emit(ProfileEffect.NavigateToSplash)
            }.onFailure { error ->
                _effect.emit(ProfileEffect.ShowError(error.message ?: "Çıkış yapılamadı."))
            }
        }
    }

    private fun handleMenuItemClick(item: String) {
        viewModelScope.launch {
            when (item) {
                "Ayarlar" -> _effect.emit(ProfileEffect.ShowError("Tema değiştirme bu ekranda kullanılabilir."))
                "Ödeme yöntemleri" -> Unit
                else -> _effect.emit(ProfileEffect.ShowError("$item için destek: destek@rencar.com"))
            }
        }
    }

    private fun handleEditProfile() {
        viewModelScope.launch {
            _effect.emit(ProfileEffect.ShowError("Profil düzenleme ekranı yakında aktif olacak."))
        }
    }

    private fun toggleTheme() {
        val newState = !_uiState.value.isDarkMode
        _uiState.update { it.copy(isDarkMode = newState) }
        ThemeManager.isDarkMode.value = newState
    }

    private fun formatDurationMinutes(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "$hours sa $minutes dk" else "$minutes dk"
    }
}
