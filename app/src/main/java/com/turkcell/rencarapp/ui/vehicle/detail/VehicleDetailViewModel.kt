package com.turkcell.rencarapp.ui.vehicle.detail

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.turkcell.rencarapp.data.vehicle.VehicleDistanceFormatter
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Route'dan gelen parametreler
    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])
    private val passedLat: Double? = savedStateHandle.get<String>("userLat")?.toDoubleOrNull()
    private val passedLng: Double? = savedStateHandle.get<String>("userLng")?.toDoubleOrNull()

    private val _uiState = MutableStateFlow(VehicleDetailUiState())
    val uiState: StateFlow<VehicleDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<VehicleDetailEffect>(Channel.BUFFERED)
    val effect: Flow<VehicleDetailEffect> = _effect.receiveAsFlow()

    init {
        onIntent(VehicleDetailIntent.LoadVehicle)
    }

    /**
     * UI'dan gelen niyetleri işler.
     */
    fun onIntent(intent: VehicleDetailIntent) {
        when (intent) {
            is VehicleDetailIntent.LoadVehicle -> loadVehicle()
            is VehicleDetailIntent.BackClicked -> {
                viewModelScope.launch { _effect.send(VehicleDetailEffect.NavigateBack) }
            }
            is VehicleDetailIntent.ReserveClicked -> {
                viewModelScope.launch {
                    _effect.send(VehicleDetailEffect.NavigateToConfirmation(vehicleId))
                }
            }
            is VehicleDetailIntent.UnlockClicked -> {
                // UI'dan kaldırıldı, artık işlevsiz.
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadVehicle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 1. Eğer haritadan konum gelmişse onu kullan, yoksa cihazdan son konumu al
            val userLat: Double?
            val userLng: Double?
            
            if (passedLat != null && passedLng != null) {
                userLat = passedLat
                userLng = passedLng
            } else {
                val lastLocation = try {
                    fusedLocationClient.lastLocation.await()
                } catch (e: Exception) {
                    null
                }
                userLat = lastLocation?.latitude
                userLng = lastLocation?.longitude
            }

            // 2. Araç detayını getir
            vehicleRepository.getById(vehicleId)
                .onSuccess { vehicle ->
                    // 3. Mesafe bilgisini hesapla
                    val distanceLabel = VehicleDistanceFormatter.formatDistance(
                        userLat = userLat,
                        userLng = userLng,
                        vehicleLat = vehicle.latitude,
                        vehicleLng = vehicle.longitude
                    )

                    _uiState.update { 
                        it.copy(
                            vehicle = vehicle,
                            distanceLabel = distanceLabel,
                            isLoading = false
                        ) 
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Araç bilgileri alınamadı."
                        ) 
                    }
                    _effect.send(VehicleDetailEffect.ShowMessage(exception.message ?: "Hata oluştu."))
                }
        }
    }
}
