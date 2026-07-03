package com.turkcell.rencarapp.ui.auth.register

data class RegisterUiState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isSendCodeEnabled: Boolean = false,
)

sealed interface RegisterIntent {
    data class PhoneChanged(val value: String) : RegisterIntent
    data object BackClicked : RegisterIntent
    data object SendCodeClicked : RegisterIntent
    data object LoginClicked : RegisterIntent
}

sealed interface RegisterEffect {
    data object NavigateBack : RegisterEffect
    data class NavigateToOtp(val phoneNumber: String) : RegisterEffect
    data object NavigateToLogin : RegisterEffect
}
