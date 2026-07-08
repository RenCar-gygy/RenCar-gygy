package com.turkcell.rencarapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.vehicle.Vehicle
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import com.turkcell.rencarapp.data.vehicle.VehicleStatus
import com.turkcell.rencarapp.data.vehicle.VehicleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MapViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val areaLabelResolver: MapAreaLabelResolver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MapEffect>(Channel.BUFFERED)
    val effect: Flow<MapEffect> = _effect.receiveAsFlow()

    init {
        loadVehicles()
    }

    fun onIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = intent.value) }
            }
            is MapIntent.CategorySelected -> selectCategory(intent.category)
            MapIntent.FilterClicked -> Unit
            MapIntent.MyLocationClicked -> focusMyLocation()
            MapIntent.MyLocationFocusHandled -> {
                _uiState.update { it.copy(shouldFocusMyLocation = false) }
            }
            is MapIntent.UserLocationUpdated -> {
                _uiState.update {
                    it.copy(
                        userLatitude = intent.latitude,
                        userLongitude = intent.longitude,
                    )
                }
                refreshAreaLabel()
            }
            MapIntent.FindNearestClicked -> findNearest()
            is MapIntent.VehiclePinClicked -> sendEffect(MapEffect.NavigateToVehicleDetail(intent.vehicleId))
        }
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = vehicleRepository.listAvailable()
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { vehicles ->
                    val pins = vehicles.map { vehicle -> vehicle.toMapPin() }
                    _uiState.update { state ->
                        state.copy(
                            vehiclePins = pins,
                            visiblePins = filterPins(pins, state.selectedCategory),
                            nearbyCount = pins.count { !it.isInUse },
                        )
                    }
                    refreshAreaLabel()
                }
                .onFailure { error ->
                    sendEffect(MapEffect.ShowError(error.message ?: "Araçlar yüklenemedi."))
                }
        }
    }

    private fun selectCategory(category: VehicleCategory) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                visiblePins = filterPins(state.vehiclePins, category),
            )
        }
        refreshAreaLabel()
    }

    private fun focusMyLocation() {
        val state = _uiState.value
        if (state.userLatitude == null || state.userLongitude == null) {
            viewModelScope.launch {
                sendEffect(MapEffect.ShowError("Konum izni verilmedi veya konum alınamadı."))
            }
            return
        }
        _uiState.update { it.copy(shouldFocusMyLocation = true) }
    }

    private fun refreshAreaLabel() {
        val state = _uiState.value
        val latitude = state.userLatitude ?: return
        val longitude = state.userLongitude ?: return

        viewModelScope.launch {
            val pins = state.visiblePins.ifEmpty { state.vehiclePins }
            val label = areaLabelResolver.resolve(
                latitude = latitude,
                longitude = longitude,
                vehiclePins = pins,
            )
            _uiState.update { it.copy(areaLabel = label) }
        }
    }

    private fun findNearest() {
        val nearest = _uiState.value.visiblePins.firstOrNull { !it.isInUse }
            ?: _uiState.value.vehiclePins.firstOrNull { !it.isInUse }
        nearest?.let { sendEffect(MapEffect.NavigateToVehicleDetail(it.id)) }
    }

    private fun sendEffect(effect: MapEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    companion object {
        private fun filterPins(
            pins: List<MapVehiclePin>,
            category: VehicleCategory,
        ): List<MapVehiclePin> =
            when (category) {
                VehicleCategory.ALL -> pins
                else -> pins.filter { it.category == category }
            }

        private fun Vehicle.toMapPin(): MapVehiclePin =
            MapVehiclePin(
                id = id,
                priceLabel = formatPriceLabel(pricePerDay),
                category = type.toCategory(),
                latitude = latitude,
                longitude = longitude,
                isInUse = status != VehicleStatus.AVAILABLE,
            )

        private fun formatPriceLabel(pricePerDay: Double): String {
            val hourlyLikePrice = (pricePerDay / 50.0).roundToInt().coerceAtLeast(1)
            return "₺$hourlyLikePrice"
        }

        private fun VehicleType.toCategory(): VehicleCategory =
            when (this) {
                VehicleType.HATCHBACK -> VehicleCategory.ECONOMIC
                VehicleType.SUV -> VehicleCategory.SUV
                VehicleType.SEDAN,
                VehicleType.STATION,
                VehicleType.MINIVAN,
                -> VehicleCategory.COMFORT
            }
    }
}
