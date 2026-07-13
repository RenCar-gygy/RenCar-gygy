package com.turkcell.rencarapp.ui.auth.register

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val referralCode: String = "",
    val isLoading: Boolean = false,
    val isRegisterEnabled: Boolean = false,
)

sealed interface RegisterIntent {
    data class FullNameChanged(val value: String) : RegisterIntent
    data class EmailChanged(val value: String) : RegisterIntent
    data class PasswordChanged(val value: String) : RegisterIntent
    data class PhoneChanged(val value: String) : RegisterIntent
    data class ReferralCodeChanged(val value: String) : RegisterIntent
    data object BackClicked : RegisterIntent
    data object RegisterClicked : RegisterIntent
    data object LoginClicked : RegisterIntent
}

sealed interface RegisterEffect {
    data object NavigateBack : RegisterEffect
    data object NavigateToLicense : RegisterEffect
    data object NavigateToMain : RegisterEffect
    data object NavigateToLogin : RegisterEffect
    data class ShowError(val message: String) : RegisterEffect
}
