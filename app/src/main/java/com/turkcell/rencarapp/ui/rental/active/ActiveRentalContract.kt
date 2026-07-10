package com.turkcell.rencarapp.ui.rental.active

data class ActiveRentalUiState(
    val rentalId: String = "",
    val vehicleName: String = "",
    val duration: String = "00:00:00",
    val currentPrice: String = "₺0,00",
    val distance: String = "0,0 km",
    val isLocked: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ActiveRentalIntent {
    data object LoadRental : ActiveRentalIntent
    data object ToggleLock : ActiveRentalIntent
    data object FinishRental : ActiveRentalIntent
}

sealed interface ActiveRentalEffect {
    data class NavigateToSummary(val rentalId: String) : ActiveRentalEffect
    data class ShowMessage(val message: String) : ActiveRentalEffect
}
