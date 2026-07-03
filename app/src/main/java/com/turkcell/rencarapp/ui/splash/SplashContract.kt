package com.turkcell.rencarapp.ui.splash

data class SplashUiState(
    val pageCount: Int = 3,
    val currentPage: Int = 0,
    val isCheckingSession: Boolean = true,
)

sealed interface SplashIntent {
    data object StartClicked : SplashIntent
    data object LoginClicked : SplashIntent
}

sealed interface SplashEffect {
    data object NavigateToOnboarding : SplashEffect
    data object NavigateToLogin : SplashEffect
    data object NavigateToLicense : SplashEffect
    data object NavigateToMain : SplashEffect
}
