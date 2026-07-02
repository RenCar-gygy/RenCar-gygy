package com.turkcell.rencarapp.ui.rental.history

sealed interface RentalHistoryIntent {
    data object LoadHistory : RentalHistoryIntent
    data class RentalClicked(val rentalId: String) : RentalHistoryIntent
}

sealed interface RentalHistoryEffect {
    data class ShowError(val message: String) : RentalHistoryEffect
    data class ShowToast(val message: String) : RentalHistoryEffect
}

data class RentalHistoryUiState(
    val isLoading: Boolean = false,
    val rentals: List<RentalUiModel> = emptyList(),
    val monthlyTripCount: Int = 0,
    val monthlyTotalSpent: Double = 0.0
)

data class RentalUiModel(
    val id: String,
    val vehicleName: String, // API'da yok, stub
    val dateText: String,
    val durationText: String,
    val distanceText: String, // API'da yok, stub
    val priceText: String
)