package com.turkcell.rencarapp.ui.rental.confirmation

import com.turkcell.rencarapp.data.vehicle.Vehicle

/**
 * Rezervasyon Onayı ekranının durumunu temsil eder.
 */
data class RentalConfirmationUiState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPlan: RentalPlan = RentalPlan.MINUTELY,
    val isTermsAccepted: Boolean = false,
    // Tasarımdaki mock veriler
    val freeReservationTime: String = "15 dk",
    val basePrice: String = "₺15,00",
    val estimatedPrice: String = "~₺135",
    val estimatedDuration: String = "30 dk",
    // Dinamik plan fiyatları
    val minutelyPriceLabel: String = "₺4,50/dk",
    val hourlyPriceLabel: String = "₺180/sa",
    val dailyPriceLabel: String = "₺1.450"
)

enum class RentalPlan {
    MINUTELY, HOURLY, DAILY
}

sealed interface RentalConfirmationIntent {
    data object LoadVehicle : RentalConfirmationIntent
    data object BackClicked : RentalConfirmationIntent
    data class PlanSelected(val plan: RentalPlan) : RentalConfirmationIntent
    data class TermsAcceptedChanged(val accepted: Boolean) : RentalConfirmationIntent
    data object CompleteReservationClicked : RentalConfirmationIntent
}

sealed interface RentalConfirmationEffect {
    data object NavigateBack : RentalConfirmationEffect
    data class NavigateToSummary(val vehicleId: String, val plan: String) : RentalConfirmationEffect
    data class ShowError(val message: String) : RentalConfirmationEffect
}
