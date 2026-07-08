package com.turkcell.rencarapp.ui.map

enum class VehicleCategory {
    ALL,
    ECONOMIC,
    COMFORT,
    SUV,
}

data class MapVehiclePin(
    val id: String,
    val priceLabel: String,
    val category: VehicleCategory,
    val latitude: Double,
    val longitude: Double,
    val isInUse: Boolean = false,
)

data class MapUiState(
    val nearbyCount: Int = 0,
    val areaLabel: String = "Konum alınıyor...",
    val searchQuery: String = "",
    val selectedCategory: VehicleCategory = VehicleCategory.ALL,
    val vehiclePins: List<MapVehiclePin> = emptyList(),
    val visiblePins: List<MapVehiclePin> = emptyList(),
    val isLoading: Boolean = true,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val shouldFocusMyLocation: Boolean = false,
)

sealed interface MapIntent {
    data class SearchQueryChanged(val value: String) : MapIntent
    data class CategorySelected(val category: VehicleCategory) : MapIntent
    data object FilterClicked : MapIntent
    data object MyLocationClicked : MapIntent
    data object MyLocationFocusHandled : MapIntent
    data class UserLocationUpdated(val latitude: Double, val longitude: Double) : MapIntent
    data object FindNearestClicked : MapIntent
    data class VehiclePinClicked(val vehicleId: String) : MapIntent
}

sealed interface MapEffect {
    data class NavigateToVehicleDetail(val vehicleId: String) : MapEffect
    data class ShowError(val message: String) : MapEffect
}
