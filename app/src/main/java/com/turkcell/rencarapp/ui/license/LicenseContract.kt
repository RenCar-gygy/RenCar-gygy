package com.turkcell.rencarapp.ui.license

enum class LicenseImageType {
    FRONT,
    BACK,
    SELFIE,
}

data class LicenseUiState(
    val activeStepIndex: Int = 0,
    val isFrontUploaded: Boolean = false,
    val isBackUploaded: Boolean = false,
    val isSelfieUploaded: Boolean = false,
    val isContinueEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val rejectReason: String? = null,
    val frontPreviewBytes: ByteArray? = null,
    val backPreviewBytes: ByteArray? = null,
    val selfiePreviewBytes: ByteArray? = null,
)

sealed interface LicenseIntent {
    data object BackClicked : LicenseIntent
    data object UploadFrontClicked : LicenseIntent
    data object UploadBackClicked : LicenseIntent
    data object UploadSelfieClicked : LicenseIntent
    data object ContinueClicked : LicenseIntent
    data class ImageCaptured(val type: LicenseImageType, val bytes: ByteArray) : LicenseIntent
}

sealed interface LicenseEffect {
    data object NavigateToLogin : LicenseEffect
    data object NavigateToMain : LicenseEffect
    data class LaunchCamera(val type: LicenseImageType) : LicenseEffect
    data class ShowError(val message: String) : LicenseEffect
}
