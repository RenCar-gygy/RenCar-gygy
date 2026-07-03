package com.turkcell.rencarapp.ui.auth.login

data class LoginUiState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isSendCodeEnabled: Boolean = false,
)

sealed interface LoginIntent {
    data class PhoneChanged(val value: String) : LoginIntent
    data object BackClicked : LoginIntent
    data object SendCodeClicked : LoginIntent
    data object RegisterClicked : LoginIntent
}

sealed interface LoginEffect {
    data object NavigateBack : LoginEffect
    data class NavigateToOtp(val phoneNumber: String) : LoginEffect
    data object NavigateToRegister : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}
