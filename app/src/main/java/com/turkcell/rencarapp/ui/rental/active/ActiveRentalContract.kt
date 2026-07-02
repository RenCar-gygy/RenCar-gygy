package com.turkcell.rencarapp.ui.rental.active

data class ActiveRentalUiState(
    val rentalId: String = "",
    val vehicleName: String = "Renault Clio",
    val duration: String = "00:24:18",
    val currentPrice: String = "₺108,00",
    val distance: String = "12,4 km",
    val isLocked: Boolean = true,
    val isLoading: Boolean = false
)

sealed interface ActiveRentalIntent {
    data object LoadRental : ActiveRentalIntent
    data object ToggleLock : ActiveRentalIntent
    data object FinishRental : ActiveRentalIntent
}

sealed interface ActiveRentalEffect {
    data object NavigateToMain : ActiveRentalEffect
    data class ShowMessage(val message: String) : ActiveRentalEffect
}
