package com.turkcell.rencarapp.ui.vehicle.detail

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.turkcell.rencarapp.data.network.ApiErrorContext
import com.turkcell.rencarapp.data.network.toUserMessage
import com.turkcell.rencarapp.data.rental.RentalPlan
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.rental.RentalStatus
import com.turkcell.rencarapp.data.reservation.ReservationRepository
import com.turkcell.rencarapp.data.vehicle.VehicleDistanceFormatter
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import com.turkcell.rencarapp.data.vehicle.VehicleStatus
import com.turkcell.rencarapp.data.rental.defaultQuoteMinutes
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
    private val reservationRepository: ReservationRepository,
    private val rentalRepository: RentalRepository,
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
            is VehicleDetailIntent.ReserveClicked -> reserveVehicle()
            is VehicleDetailIntent.PlanChanged -> {
                _uiState.update { it.copy(selectedPlan = intent.plan) }
            }
            is VehicleDetailIntent.UnlockClicked -> {
                // UI'dan kaldırıldı, artık işlevsiz.
            }
        }
    }

    private fun reserveVehicle() {
        if (_uiState.value.isReserving) return

        if (!_uiState.value.isReservable) {
            viewModelScope.launch {
                _effect.send(
                    VehicleDetailEffect.ShowMessage(
                        _uiState.value.unavailableMessage
                            ?: "Bu araç şu anda kiralanamaz."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isReserving = true) }

            val selectedPlan = _uiState.value.selectedPlan
            val preflightError = resolveReservationBlocker()
            if (preflightError != null) {
                when (preflightError) {
                    is ReservationBlocker.ResumeConfirmation -> {
                        _uiState.update { it.copy(isReserving = false) }
                        _effect.send(VehicleDetailEffect.NavigateToConfirmation(vehicleId, selectedPlan))
                    }
                    is ReservationBlocker.OpenRental -> {
                        _uiState.update { it.copy(isReserving = false) }
                        _effect.send(
                            VehicleDetailEffect.ShowMessage(
                                "Devam eden bir kiralamanız var. Önce tamamlayın veya iptal edin."
                            )
                        )
                        _effect.send(VehicleDetailEffect.NavigateToActiveRental(preflightError.rentalId))
                    }
                    is ReservationBlocker.Message -> {
                        _uiState.update { it.copy(isReserving = false) }
                        _effect.send(VehicleDetailEffect.ShowMessage(preflightError.text))
                    }
                }
                return@launch
            }

            reservationRepository.create(vehicleId)
                .onSuccess {
                    _uiState.update { it.copy(isReserving = false) }
                    _effect.send(VehicleDetailEffect.NavigateToConfirmation(vehicleId, selectedPlan))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isReserving = false) }
                    _effect.send(
                        VehicleDetailEffect.ShowMessage(
                            error.toUserMessage(ApiErrorContext.RESERVATION_CREATE)
                        )
                    )
                }
        }
    }

    private suspend fun resolveReservationBlocker(): ReservationBlocker? {
        reservationRepository.getActive().getOrNull()?.let { reservation ->
            return if (reservation.vehicleId == vehicleId) {
                ReservationBlocker.ResumeConfirmation
            } else {
                ReservationBlocker.Message(
                    "Başka bir araç için aktif rezervasyonunuz var (${reservation.vehicle.plate}). " +
                        "Önce rezervasyon onay ekranından geri dönerek iptal edin."
                )
            }
        }

        rentalRepository.getActive().getOrNull()?.let { activeRental ->
            return ReservationBlocker.OpenRental(activeRental.id)
        }

        rentalRepository.listMine().getOrNull()
            ?.firstOrNull { it.status == RentalStatus.PREPARING }
            ?.let { preparingRental ->
                return ReservationBlocker.OpenRental(preparingRental.id)
            }

        return null
    }

    private sealed interface ReservationBlocker {
        data object ResumeConfirmation : ReservationBlocker
        data class OpenRental(val rentalId: String) : ReservationBlocker
        data class Message(val text: String) : ReservationBlocker
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

                    val isReservable = vehicle.status == VehicleStatus.AVAILABLE
                    val unavailableMessage = when (vehicle.status) {
                        VehicleStatus.RENTED -> "Bu araç şu anda kirada."
                        VehicleStatus.RESERVED -> "Bu araç başka bir kullanıcı tarafından rezerve edildi."
                        VehicleStatus.MAINTENANCE -> "Bu araç bakımda."
                        else -> null
                    }

                    _uiState.update {
                        it.copy(
                            vehicle = vehicle,
                            distanceLabel = distanceLabel,
                            isLoading = false,
                            isReservable = isReservable,
                            unavailableMessage = unavailableMessage,
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.toUserMessage(ApiErrorContext.VEHICLE_DETAIL)
                        ) 
                    }
                    _effect.send(
                        VehicleDetailEffect.ShowMessage(
                            exception.toUserMessage(ApiErrorContext.VEHICLE_DETAIL)
                        )
                    )
                }
        }
    }
}
