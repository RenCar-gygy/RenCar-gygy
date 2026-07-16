package com.turkcell.rencarapp.ui.vehicle.detail

import com.turkcell.rencarapp.data.rental.RentalPlan
import com.turkcell.rencarapp.data.vehicle.Vehicle

/**
 * Vehicle Detail ekranının durumunu temsil eder.
 */
data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val isReserving: Boolean = false,
    val error: String? = null,
    val distanceLabel: String = "",
    val selectedPlan: RentalPlan = RentalPlan.PER_MINUTE,
    val isReservable: Boolean = true,
    val unavailableMessage: String? = null,
)

/**
 * Kullanıcı niyetlerini temsil eder.
 */
sealed interface VehicleDetailIntent {
    data object LoadVehicle : VehicleDetailIntent
    data object BackClicked : VehicleDetailIntent
    data object ReserveClicked : VehicleDetailIntent
    data class PlanChanged(val plan: RentalPlan, val minutes: Int) : VehicleDetailIntent
    data object UnlockClicked : VehicleDetailIntent
}

/**
 * Tek seferlik olayları temsil eder.
 */
sealed interface VehicleDetailEffect {
    data object NavigateBack : VehicleDetailEffect
    data class NavigateToConfirmation(val vehicleId: String, val plan: RentalPlan) : VehicleDetailEffect
    data class NavigateToActiveRental(val rentalId: String) : VehicleDetailEffect
    data class ShowMessage(val message: String) : VehicleDetailEffect
}
