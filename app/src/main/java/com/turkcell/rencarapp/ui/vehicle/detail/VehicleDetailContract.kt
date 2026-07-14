package com.turkcell.rencarapp.ui.vehicle.detail

import com.turkcell.rencarapp.data.vehicle.Vehicle

/**
 * Vehicle Detail ekranının durumunu temsil eder.
 */
data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val isReserving: Boolean = false,
    val error: String? = null,
    val distanceLabel: String = "", // Dinamik mesafe bilgisi
    val estimatedPrice: String? = null, // API'den gelen tahmini fiyat
)

/**
 * Kullanıcı niyetlerini temsil eder.
 */
sealed interface VehicleDetailIntent {
    data object LoadVehicle : VehicleDetailIntent
    data object BackClicked : VehicleDetailIntent
    data object ReserveClicked : VehicleDetailIntent
    data class PlanChanged(val plan: com.turkcell.rencarapp.data.network.dto.RentalPlan, val minutes: Int) : VehicleDetailIntent
    data object UnlockClicked : VehicleDetailIntent
}

/**
 * Tek seferlik olayları temsil eder.
 */
sealed interface VehicleDetailEffect {
    data object NavigateBack : VehicleDetailEffect
    data class NavigateToConfirmation(val vehicleId: String) : VehicleDetailEffect
    data class ShowMessage(val message: String) : VehicleDetailEffect
}
