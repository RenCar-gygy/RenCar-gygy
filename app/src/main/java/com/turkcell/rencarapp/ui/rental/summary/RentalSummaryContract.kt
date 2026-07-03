package com.turkcell.rencarapp.ui.rental.summary

sealed interface RentalSummaryIntent {
    data object PayClicked : RentalSummaryIntent
    data object ChangeCardClicked : RentalSummaryIntent
}

sealed interface RentalSummaryEffect {
    data class NavigateToDeliveryPhotos(val vehicleId: String) : RentalSummaryEffect
    data class ShowToast(val message: String) : RentalSummaryEffect
    data class ShowError(val message: String) : RentalSummaryEffect
}

data class RentalSummaryUiState(
    val isLoading: Boolean = false,
    val vehicleId: String = "",
    val vehicleName: String = "",
    val plate: String = "",
    val durationText: String = "24 dk",
    val distanceText: String = "12,4 km",
    val rentalFee: String = "₺108,00",
    val startFee: String = "₺15,00",
    val serviceFee: String = "₺7,50",
    val discount: String = "-₺20,00",
    val totalFee: String = "₺110,50",
    val cardBrand: String = "VISA",
    val cardLast4: String = "4291"
)