package com.turkcell.rencarapp.ui.rental.history

data class RentalHistoryUiState(
    val isLoading: Boolean = false,
    val rentals: List<RentalUiModel> = emptyList(),
    val monthlyTripCount: Int = 0,
    val monthlyTotalSpent: Double = 0.0
)

data class RentalUiModel(
    val id: String,
    val vehicleName: String,
    val dateText: String,
    val durationText: String,
    val distanceText: String,
    val priceText: String
)

sealed interface RentalHistoryIntent {
    data object LoadHistory : RentalHistoryIntent
    data class RentalClicked(val id: String) : RentalHistoryIntent
}

sealed interface RentalHistoryEffect {
    data class ShowToast(val message: String) : RentalHistoryEffect
    data class ShowError(val message: String) : RentalHistoryEffect
}