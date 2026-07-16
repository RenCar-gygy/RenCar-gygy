package com.turkcell.rencarapp.ui.rental.confirmation

import com.turkcell.rencarapp.data.rental.RentalPlan
import com.turkcell.rencarapp.data.vehicle.Vehicle

data class RentalConfirmationUiState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPlan: RentalPlan = RentalPlan.PER_MINUTE,
    val isTermsAccepted: Boolean = false,
    val freeReservationTime: String = "15 dk",
    val basePriceLabel: String = "₺0,00",
    val estimatedPriceLabel: String = "₺0,00",
    val serviceFeeLabel: String = "₺0,00",
    val estimatedDuration: String = "30 dk",
    val minutelyPriceLabel: String = "₺4,50/dk",
    val hourlyPriceLabel: String = "₺180/sa",
    val dailyPriceLabel: String = "₺1.450"
)

sealed interface RentalConfirmationIntent {
    data object LoadVehicle : RentalConfirmationIntent
    data object BackClicked : RentalConfirmationIntent
    data class PlanSelected(val plan: RentalPlan) : RentalConfirmationIntent
    data class TermsAcceptedChanged(val accepted: Boolean) : RentalConfirmationIntent
    data object CompleteReservationClicked : RentalConfirmationIntent
}

sealed interface RentalConfirmationEffect {
    data object NavigateBack : RentalConfirmationEffect
    data class NavigateToActiveRental(val rentalId: String) : RentalConfirmationEffect
    data class ShowError(val message: String) : RentalConfirmationEffect
}
