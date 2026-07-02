package com.turkcell.rencarapp.ui.onboarding

data class OnboardingUiState(
    val pageCount: Int = 3,
    /** Splash 1. sayfayı gösterir; onboarding 2 ve 3. sayfalarla devam eder (0-indexed: 1..2). */
    val currentPage: Int = 1,
)

sealed interface OnboardingIntent {
    data class PageChanged(val page: Int) : OnboardingIntent
    data object ContinueClicked : OnboardingIntent
    data object FinishClicked : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToRegister : OnboardingEffect
}
