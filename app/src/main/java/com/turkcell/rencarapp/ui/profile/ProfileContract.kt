package com.turkcell.rencarapp.ui.profile

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object LogoutClicked : ProfileIntent
    data class MenuItemClicked(val item: String) : ProfileIntent
}

sealed interface ProfileEffect {
    data object NavigateToSplash : ProfileEffect
    data class ShowError(val message: String) : ProfileEffect
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val fullName: String = "",
    val phone: String = "",
    val isLicenseVerified: Boolean = false
)