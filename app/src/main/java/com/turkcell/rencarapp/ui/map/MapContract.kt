package com.turkcell.rencarapp.ui.map

import com.turkcell.rencarapp.data.vehicle.VehicleType

enum class VehicleCategory {
    ALL,
    ECONOMIC,
    COMFORT,
    SUV,
}

/** Konum izni: tam (precise) veya yaklaşık (approximate). */
enum class MapLocationPrecision {
    PRECISE,
    APPROXIMATE,
}

enum class MapActiveSessionType {
    RENTAL,
    RESERVATION,
}

data class MapActiveSession(
    val type: MapActiveSessionType,
    val rentalId: String? = null,
    val vehicleId: String? = null,
    val title: String,
    val subtitle: String,
)

data class MapVehiclePin(
    val id: String,
    val priceLabel: String,
    val brand: String,
    val model: String,
    val plate: String,
    val category: VehicleCategory,
    val vehicleType: VehicleType,
    val latitude: Double,
    val longitude: Double,
    val isInUse: Boolean = false,
) {
    val displayName: String get() = "$brand $model"
}

data class MapUiState(
    val nearbyCount: Int = 0,
    val areaLabel: String = "Konum alınıyor...",
    val searchQuery: String = "",
    val isLocationSearchActive: Boolean = false,
    val searchAreaLatitude: Double? = null,
    val searchAreaLongitude: Double? = null,
    val searchAreaLabel: String? = null,
    val selectedCategory: VehicleCategory = VehicleCategory.ALL,
    val isFilterSheetVisible: Boolean = false,
    val showOnlyAvailable: Boolean = false,
    val selectedVehicleTypes: Set<VehicleType> = emptySet(),
    val vehiclePins: List<MapVehiclePin> = emptyList(),
    val visiblePins: List<MapVehiclePin> = emptyList(),
    val isLoading: Boolean = true,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val locationPrecision: MapLocationPrecision = MapLocationPrecision.APPROXIMATE,
    val shouldFocusMyLocation: Boolean = false,
    val shouldFocusVisiblePins: Boolean = false,
    val shouldFocusSearchArea: Boolean = false,
    val activeSession: MapActiveSession? = null,
)

sealed interface MapIntent {
    data class SearchQueryChanged(val value: String) : MapIntent
    data object SearchSubmitted : MapIntent
    data class CategorySelected(val category: VehicleCategory) : MapIntent
    data object FilterClicked : MapIntent
    data object FilterSheetDismissed : MapIntent
    data class ShowOnlyAvailableChanged(val enabled: Boolean) : MapIntent
    data class VehicleTypeFilterToggled(val type: VehicleType) : MapIntent
    data object MyLocationClicked : MapIntent
    data object MyLocationFocusHandled : MapIntent
    data object VisiblePinsFocusHandled : MapIntent
    data object SearchAreaFocusHandled : MapIntent
    data class UserLocationUpdated(
        val latitude: Double,
        val longitude: Double,
        val isPreciseLocation: Boolean,
    ) : MapIntent
    data object FindNearestClicked : MapIntent
    data object ActiveSessionClicked : MapIntent
    data object RefreshActiveSession : MapIntent
    data class VehiclePinClicked(val vehicleId: String) : MapIntent
}

sealed interface MapEffect {
    data class NavigateToVehicleDetail(
        val vehicleId: String,
        val userLat: Double? = null,
        val userLng: Double? = null,
    ) : MapEffect
    data class NavigateToActiveRental(val rentalId: String) : MapEffect
    data class NavigateToConfirmation(val vehicleId: String) : MapEffect
    data class ShowError(val message: String) : MapEffect
}
