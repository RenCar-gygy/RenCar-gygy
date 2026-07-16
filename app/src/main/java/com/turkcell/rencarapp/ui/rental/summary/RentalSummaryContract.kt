package com.turkcell.rencarapp.ui.rental.summary

sealed interface RentalSummaryIntent {
    data object PayClicked : RentalSummaryIntent
    data object ChangeCardClicked : RentalSummaryIntent
}

sealed interface RentalSummaryEffect {
    data object NavigateToHome : RentalSummaryEffect // Ödeme bitince ana sayfaya dönmeli
    data class ShowToast(val message: String) : RentalSummaryEffect
    data class ShowError(val message: String) : RentalSummaryEffect
}

data class RentalSummaryUiState(
    val isLoading: Boolean = false,
    val rentalId: String = "",
    val vehicleName: String = "",
    val plate: String = "",
    val durationText: String = "",
    val distanceText: String = "",
    val rentalFee: String = "",
    val startFee: String = "",
    val serviceFee: String = "",
    val discount: String = "",
    val totalFee: String = "",
    val cardBrand: String = "VISA",
    val cardLast4: String = "4291"
)