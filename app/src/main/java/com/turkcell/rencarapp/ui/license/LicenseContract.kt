package com.turkcell.rencarapp.ui.license

data class LicenseUiState(
    val activeStepIndex: Int = 0,
    val isFrontUploaded: Boolean = true,
    val isBackUploaded: Boolean = false,
    val isSelfieUploaded: Boolean = false,
    val isContinueEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val rejectReason: String? = null,
)

sealed interface LicenseIntent {
    data object BackClicked : LicenseIntent
    data object UploadBackClicked : LicenseIntent
    data object UploadSelfieClicked : LicenseIntent
    data object ContinueClicked : LicenseIntent
}

sealed interface LicenseEffect {
    data object NavigateBack : LicenseEffect
    data object NavigateToMain : LicenseEffect
    data class ShowError(val message: String) : LicenseEffect
}
