package com.turkcell.rencarapp.ui.auth.otp

data class OtpUiState(
    val phoneNumber: String = "",
    val formattedPhone: String = "",
    val code: String = "",
    val remainingSeconds: Int = RESEND_SECONDS,
    val isResendEnabled: Boolean = false,
    val isVerifyEnabled: Boolean = false,
    val isLoading: Boolean = false,
) {
    companion object {
        const val RESEND_SECONDS = 42
        const val CODE_LENGTH = 6
    }
}

sealed interface OtpIntent {
    data class CodeChanged(val value: String) : OtpIntent
    data object BackClicked : OtpIntent
    data object ResendClicked : OtpIntent
    data object VerifyClicked : OtpIntent
    data object ChangePhoneClicked : OtpIntent
}

sealed interface OtpEffect {
    data object NavigateBack : OtpEffect
    data object NavigateToLogin : OtpEffect
    data object NavigateToLicense : OtpEffect
    data object NavigateToMain : OtpEffect
    data class ShowError(val message: String) : OtpEffect
}
