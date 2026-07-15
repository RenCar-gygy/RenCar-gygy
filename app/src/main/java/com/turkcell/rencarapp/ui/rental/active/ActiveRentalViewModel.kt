package com.turkcell.rencarapp.ui.rental.active

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.network.ApiErrorContext
import com.turkcell.rencarapp.data.network.toUserMessage
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.rental.RentalStatus
import com.turkcell.rencarapp.data.rental.RideLocationClient
import com.turkcell.rencarapp.data.reservation.ReservationRepository
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ActiveRentalViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val reservationRepository: ReservationRepository,
    private val vehicleRepository: VehicleRepository,
    private val rideLocationClient: RideLocationClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navRentalId: String? = savedStateHandle["rentalId"]
    private var awaitingRideStart: Boolean = !navRentalId.isNullOrBlank()

    private val _uiState = MutableStateFlow(
        ActiveRentalUiState(
            rentalId = navRentalId.orEmpty(),
            isPreparingRental = awaitingRideStart,
            isLocked = true,
        )
    )
    val uiState: StateFlow<ActiveRentalUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ActiveRentalEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var pollingJob: Job? = null
    private var locationJob: Job? = null

    init {
        startPolling()
        viewModelScope.launch {
            savedStateHandle.getStateFlow(RIDE_STARTED_KEY, false).collect { started ->
                if (started) {
                    savedStateHandle[RIDE_STARTED_KEY] = false
                    markRideStarted()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        locationJob?.cancel()
    }

    fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            is ActiveRentalIntent.LoadRental -> startPolling()
            is ActiveRentalIntent.ToggleLock -> handleToggleLock()
            is ActiveRentalIntent.FinishRental -> finishRental()
            is ActiveRentalIntent.CancelRentalClicked -> cancelRental()
        }
    }

    private fun handleToggleLock() {
        if (awaitingRideStart && _uiState.value.isLocked) {
            beginRide()
            return
        }

        if (_uiState.value.isPreparingRental && _uiState.value.isLocked) {
            beginRide()
            return
        }

        _uiState.update { it.copy(isLocked = !it.isLocked) }
        viewModelScope.launch {
            val msg = if (_uiState.value.isLocked) "Araç kilitlendi" else "Araç kilidi açıldı, iyi yolculuklar!"
            _effect.send(ActiveRentalEffect.ShowMessage(msg))
        }
    }

    private fun beginRide() {
        val rentalId = _uiState.value.rentalId
        if (rentalId.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rentalRepository.getById(rentalId)
                .onSuccess { rental ->
                    when (rental.status) {
                        RentalStatus.PREPARING -> navigateToStartPhotos()
                        RentalStatus.ACTIVE -> markRideStarted()
                        else -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _effect.send(ActiveRentalEffect.ShowMessage("Kiralama bu aşamada başlatılamıyor."))
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        ActiveRentalEffect.ShowMessage(
                            error.toUserMessage(ApiErrorContext.RENTAL_START)
                        )
                    )
                }
        }
    }

    private suspend fun navigateToStartPhotos() {
        _uiState.update { it.copy(isLoading = false) }
        _effect.send(
            ActiveRentalEffect.NavigateToStartPhotos(
                rentalId = _uiState.value.rentalId,
                vehicleName = _uiState.value.vehicleName,
                vehiclePlate = _uiState.value.vehiclePlate
            )
        )
    }

    private fun markRideStarted() {
        awaitingRideStart = false
        _uiState.update {
            it.copy(
                isPreparingRental = false,
                canCancelRental = false,
                isLocked = false,
                isLoading = false,
                error = null,
            )
        }
        startVehicleLocationTracking()
        viewModelScope.launch {
            _effect.send(ActiveRentalEffect.ShowMessage("Araç kilidi açıldı, iyi yolculuklar!"))
        }
    }

    private fun cancelRental() {
        val rentalId = _uiState.value.rentalId
        if (rentalId.isBlank() || _uiState.value.isLoading || !_uiState.value.canCancelRental) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rentalRepository.getById(rentalId)
                .onSuccess { rental ->
                    if (rental.status != RentalStatus.PREPARING) {
                        _uiState.update { it.copy(isLoading = false, canCancelRental = false) }
                        _effect.send(
                            ActiveRentalEffect.ShowMessage(
                                "Yalnızca henüz başlamamış kiralama iptal edilebilir. Aktif yolculuğu bitirmek için «Kiralamayı Bitir» kullanın."
                            )
                        )
                        return@launch
                    }
                    rentalRepository.cancel(rentalId)
                        .onSuccess {
                            _uiState.update { it.copy(isLoading = false) }
                            _effect.send(ActiveRentalEffect.NavigateBackAfterCancel)
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(isLoading = false) }
                            _effect.send(
                                ActiveRentalEffect.ShowMessage(
                                    error.toUserMessage(ApiErrorContext.RENTAL_CANCEL)
                                )
                            )
                        }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        ActiveRentalEffect.ShowMessage(
                            error.toUserMessage(ApiErrorContext.RENTAL_CANCEL)
                        )
                    )
                }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                rentalRepository.getActive()
                    .onSuccess { activeRental ->
                        val status = activeRental.status.toRentalStatus()
                        val isServerPreparing = status == RentalStatus.PREPARING
                        val showAwaitingUnlock = awaitingRideStart && status == RentalStatus.ACTIVE

                        if (showAwaitingUnlock || isServerPreparing) {
                            _uiState.update {
                                it.copy(
                                    rentalId = activeRental.id,
                                    vehicleName = "${activeRental.vehicle.brand} ${activeRental.vehicle.model}",
                                    vehiclePlate = activeRental.vehicle.plate,
                                    duration = "00:00:00",
                                    currentPrice = "₺0,00",
                                    distance = "0,0 km",
                                    isReservationActive = false,
                                    isPreparingRental = isServerPreparing,
                                    canCancelRental = isServerPreparing,
                                    isVehicleLocationPending = it.vehicleLatitude == null,
                                    remainingReservationSeconds = null,
                                    isLoading = false,
                                    error = null,
                                    isLocked = true,
                                )
                            }
                        } else {
                            startVehicleLocationTracking()
                            _uiState.update { currentState ->
                                currentState.copy(
                                    rentalId = activeRental.id,
                                    vehicleName = "${activeRental.vehicle.brand} ${activeRental.vehicle.model}",
                                    vehiclePlate = activeRental.vehicle.plate,
                                    duration = formatSeconds(activeRental.elapsedSeconds),
                                    currentPrice = String.format(Locale.getDefault(), "₺%.2f", activeRental.currentCost),
                                    distance = if (currentState.isLocked) currentState.distance else String.format(Locale.getDefault(), "%.1f km", activeRental.distanceKm),
                                    isReservationActive = false,
                                    isPreparingRental = false,
                                    canCancelRental = false,
                                    isVehicleLocationPending = currentState.vehicleLatitude == null,
                                    remainingReservationSeconds = null,
                                    isLoading = false,
                                    error = null,
                                )
                            }
                        }
                    }
                    .onFailure { activeError ->
                        if (!awaitingRideStart) {
                            stopVehicleLocationTracking()
                        }
                        loadPreparingOrReservation(activeError)
                    }
                delay(2000)
            }
        }
    }

    private suspend fun loadPreparingOrReservation(activeError: Throwable) {
        val rentalId = navRentalId?.takeIf { it.isNotBlank() } ?: _uiState.value.rentalId
        if (rentalId.isNotBlank()) {
            rentalRepository.getById(rentalId)
                .onSuccess { rental ->
                    when (rental.status) {
                        RentalStatus.PREPARING -> {
                            loadPreparingRental(rental.vehicleId, rental.id)
                            return
                        }
                        RentalStatus.ACTIVE -> {
                            if (awaitingRideStart) {
                                loadAwaitingUnlockRental(rental.vehicleId, rental.id)
                            }
                            return
                        }
                        else -> Unit
                    }
                }
        }

        reservationRepository.getActive()
            .onSuccess { reservation ->
                _uiState.update {
                    it.copy(
                        vehicleName = "${reservation.vehicle.brand} ${reservation.vehicle.model}",
                        vehiclePlate = reservation.vehicle.plate,
                        duration = formatCountdown(reservation.remainingSeconds),
                        currentPrice = "₺0,00",
                        distance = "0,0 km",
                        isReservationActive = true,
                        isPreparingRental = false,
                        isVehicleLocationPending = false,
                        remainingReservationSeconds = reservation.remainingSeconds,
                        vehicleLatitude = reservation.vehicle.latitude,
                        vehicleLongitude = reservation.vehicle.longitude,
                        isLoading = false,
                        error = null,
                    )
                }
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        error = activeError.toUserMessage(ApiErrorContext.RENTAL_ACTIVE),
                        isLoading = false,
                    )
                }
            }
    }

    private suspend fun loadPreparingRental(vehicleId: String, rentalId: String) {
        vehicleRepository.getById(vehicleId)
            .onSuccess { vehicle ->
                _uiState.update {
                    it.copy(
                        rentalId = rentalId,
                        vehicleName = "${vehicle.brand} ${vehicle.model}",
                        vehiclePlate = vehicle.plate,
                        duration = "00:00:00",
                        currentPrice = "₺0,00",
                        distance = "0,0 km",
                        isReservationActive = false,
                        isPreparingRental = true,
                        canCancelRental = true,
                        isVehicleLocationPending = false,
                        remainingReservationSeconds = null,
                        vehicleLatitude = vehicle.latitude,
                        vehicleLongitude = vehicle.longitude,
                        isLoading = false,
                        error = null,
                        isLocked = true,
                    )
                }
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        rentalId = rentalId,
                        isPreparingRental = true,
                        canCancelRental = true,
                        duration = "00:00:00",
                        currentPrice = "₺0,00",
                        distance = "0,0 km",
                        isLoading = false,
                        error = null,
                        isLocked = true,
                    )
                }
            }
    }

    private suspend fun loadAwaitingUnlockRental(vehicleId: String, rentalId: String) {
        vehicleRepository.getById(vehicleId)
            .onSuccess { vehicle ->
                _uiState.update {
                    it.copy(
                        rentalId = rentalId,
                        vehicleName = "${vehicle.brand} ${vehicle.model}",
                        vehiclePlate = vehicle.plate,
                        duration = "00:00:00",
                        currentPrice = "₺0,00",
                        distance = "0,0 km",
                        isReservationActive = false,
                        isPreparingRental = false,
                        canCancelRental = false,
                        isVehicleLocationPending = false,
                        remainingReservationSeconds = null,
                        vehicleLatitude = vehicle.latitude,
                        vehicleLongitude = vehicle.longitude,
                        isLoading = false,
                        error = null,
                        isLocked = true,
                    )
                }
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        rentalId = rentalId,
                        isPreparingRental = false,
                        canCancelRental = false,
                        duration = "00:00:00",
                        currentPrice = "₺0,00",
                        distance = "0,0 km",
                        isLoading = false,
                        error = null,
                        isLocked = true,
                    )
                }
            }
    }

    private fun startVehicleLocationTracking() {
        if (locationJob?.isActive == true) return

        locationJob = viewModelScope.launch {
            rideLocationClient.vehiclePositionStream()
                .collect { point ->
                    if (!_uiState.value.isLocked) {
                        _uiState.update {
                            it.copy(
                                vehicleLatitude = point.latitude,
                                vehicleLongitude = point.longitude,
                                isVehicleLocationPending = false,
                            )
                        }
                    }
                }
        }
    }

    private fun stopVehicleLocationTracking() {
        locationJob?.cancel()
        locationJob = null
    }

    private fun formatCountdown(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun formatSeconds(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun finishRental() {
        val currentRentalId = _uiState.value.rentalId
        if (currentRentalId.isBlank() || awaitingRideStart || _uiState.value.isPreparingRental) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rentalRepository.finish(currentRentalId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(ActiveRentalEffect.NavigateToSummary(rentalId = currentRentalId))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        ActiveRentalEffect.ShowMessage(
                            error.toUserMessage(ApiErrorContext.RENTAL_FINISH)
                        )
                    )
                }
        }
    }

    companion object {
        const val RIDE_STARTED_KEY = "rideStarted"
    }
}

private fun String.toRentalStatus(): RentalStatus? =
    when (uppercase()) {
        RentalStatus.PREPARING.name -> RentalStatus.PREPARING
        RentalStatus.ACTIVE.name -> RentalStatus.ACTIVE
        RentalStatus.COMPLETED.name -> RentalStatus.COMPLETED
        RentalStatus.CANCELLED.name -> RentalStatus.CANCELLED
        else -> null
    }
