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
            MapIntent.MyLocationClicked -> Unit
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
                    val pins = vehicles.mapIndexed { index, vehicle -> vehicle.toMapPin(index) }
                    _uiState.update { state ->
                        state.copy(
                            vehiclePins = pins,
                            visiblePins = filterPins(pins, state.selectedCategory),
                            nearbyCount = pins.count { !it.isInUse },
                        )
                    }
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
        private val pinOffsets = listOf(
            0.22f to 0.28f,
            0.62f to 0.22f,
            0.48f to 0.42f,
            0.35f to 0.55f,
            0.78f to 0.48f,
        )

        private fun filterPins(
            pins: List<MapVehiclePin>,
            category: VehicleCategory,
        ): List<MapVehiclePin> =
            when (category) {
                VehicleCategory.ALL -> pins
                else -> pins.filter { it.category == category }
            }

        private fun Vehicle.toMapPin(index: Int): MapVehiclePin {
            val (offsetX, offsetY) = pinOffsets.getOrElse(index) {
                pinOffsets[index % pinOffsets.size]
            }
            return MapVehiclePin(
                id = id,
                priceLabel = formatPriceLabel(pricePerDay),
                category = type.toCategory(),
                offsetXFraction = offsetX,
                offsetYFraction = offsetY,
                isInUse = status != VehicleStatus.AVAILABLE,
            )
        }

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
