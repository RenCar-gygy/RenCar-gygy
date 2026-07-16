package com.turkcell.rencarapp.ui.profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val fullName: String = "",
    val phone: String = "",
    val referralCode: String = "",
    val isLicenseVerified: Boolean = false,
    val isDarkMode: Boolean = false,
    val monthlyTripCount: Int = 0,
    val monthlyTotalSpent: String = "",
    val monthlyTotalKm: String = "",
    val monthlyTotalMinutes: String = "",
)

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object LogoutClicked : ProfileIntent
    data object EditProfileClicked : ProfileIntent
    data object CopyReferralCodeClicked : ProfileIntent
    data class MenuItemClicked(val item: String) : ProfileIntent
    data object ThemeToggleClicked : ProfileIntent
}

sealed interface ProfileEffect {
    data object NavigateToSplash : ProfileEffect
    data class ShowError(val message: String) : ProfileEffect
    data class ShowMessage(val message: String) : ProfileEffect
    data class CopyReferralCode(val code: String) : ProfileEffect
}