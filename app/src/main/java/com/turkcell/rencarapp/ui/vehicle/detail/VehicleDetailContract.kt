package com.turkcell.rencarapp.ui.vehicle.detail

import com.turkcell.rencarapp.data.vehicle.Vehicle

/**
 * Vehicle Detail ekranının durumunu temsil eder.
 */
data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val distanceLabel: String = "", // Dinamik mesafe bilgisi
    // API'da olmayan ancak tasarımda bulunan mock alanlar
    val fuelLevel: String = "%72",
    val range: String = "~480 km",
    val transmission: String = "Manuel",
    val seatingCapacity: String = "5 kişi"
)

/**
 * Kullanıcı niyetlerini temsil eder.
 */
sealed interface VehicleDetailIntent {
    data object LoadVehicle : VehicleDetailIntent
    data object BackClicked : VehicleDetailIntent
    data object ReserveClicked : VehicleDetailIntent
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
