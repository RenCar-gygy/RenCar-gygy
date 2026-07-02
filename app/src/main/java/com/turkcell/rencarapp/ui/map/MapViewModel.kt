package com.turkcell.rencarapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    private val allPins = defaultVehiclePins()

    private val _uiState = MutableStateFlow(
        MapUiState(
            vehiclePins = allPins,
            visiblePins = filterPins(allPins, VehicleCategory.ALL),
        ),
    )
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MapEffect>(Channel.BUFFERED)
    val effect: Flow<MapEffect> = _effect.receiveAsFlow()

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
        private fun filterPins(
            pins: List<MapVehiclePin>,
            category: VehicleCategory,
        ): List<MapVehiclePin> =
            when (category) {
                VehicleCategory.ALL -> pins
                else -> pins.filter { it.category == category }
            }

        private fun defaultVehiclePins(): List<MapVehiclePin> = listOf(
            MapVehiclePin("1", "₺28", VehicleCategory.ECONOMIC, 0.22f, 0.28f),
            MapVehiclePin("2", "₺38", VehicleCategory.COMFORT, 0.62f, 0.22f),
            MapVehiclePin("3", "₺32", VehicleCategory.SUV, 0.48f, 0.42f),
            MapVehiclePin("4", "₺26", VehicleCategory.ECONOMIC, 0.35f, 0.55f),
            MapVehiclePin("5", "Kull...", VehicleCategory.COMFORT, 0.78f, 0.48f, isInUse = true),
        )
    }
}
