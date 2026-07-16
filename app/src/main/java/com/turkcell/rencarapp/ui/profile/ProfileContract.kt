package com.turkcell.rencarapp.ui.profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val fullName: String = "",
    val phone: String = "",
    val isLicenseVerified: Boolean = false,
    val isDarkMode: Boolean = false
)

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object LogoutClicked : ProfileIntent
    data object EditProfileClicked : ProfileIntent
    data class MenuItemClicked(val item: String) : ProfileIntent
    data object ThemeToggleClicked : ProfileIntent
}

sealed interface ProfileEffect {
    data object NavigateToSplash : ProfileEffect
    data object NavigateToWallet : ProfileEffect // YENİ: Cüzdana gitme efekti
    data class ShowError(val message: String) : ProfileEffect
}