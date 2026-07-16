package com.turkcell.rencarapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.rental.RentalStatus
import com.turkcell.rencarapp.data.reservation.ReservationRepository
import com.turkcell.rencarapp.data.vehicle.Vehicle
import com.turkcell.rencarapp.data.vehicle.VehiclePriceFormatter
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import com.turkcell.rencarapp.data.vehicle.VehicleSegment
import com.turkcell.rencarapp.data.vehicle.VehicleStatus
import com.turkcell.rencarapp.data.vehicle.VehicleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class MapViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val rentalRepository: RentalRepository,
    private val reservationRepository: ReservationRepository,
    private val areaLabelResolver: MapAreaLabelResolver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MapEffect>(Channel.BUFFERED)
    val effect: Flow<MapEffect> = _effect.receiveAsFlow()

    private var loadVehiclesJob: Job? = null
    private var refreshAreaLabelJob: Job? = null

    init {
        loadVehicles()
        refreshActiveSession()
    }

    fun onIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = intent.value) }
            }
            MapIntent.SearchSubmitted -> submitLocationSearch()
            is MapIntent.CategorySelected -> selectCategory(intent.category)
            MapIntent.FilterClicked -> {
                _uiState.update { it.copy(isFilterSheetVisible = !it.isFilterSheetVisible) }
            }
            MapIntent.FilterSheetDismissed -> {
                _uiState.update { it.copy(isFilterSheetVisible = false) }
            }
            is MapIntent.ShowOnlyAvailableChanged -> {
                _uiState.update { it.copy(showOnlyAvailable = intent.enabled) }
                applyFilters(triggerFocus = true)
            }
            is MapIntent.VehicleTypeFilterToggled -> toggleVehicleTypeFilter(intent.type)
            MapIntent.MyLocationClicked -> focusMyLocation()
            MapIntent.MyLocationFocusHandled -> {
                _uiState.update { it.copy(shouldFocusMyLocation = false) }
            }
            MapIntent.VisiblePinsFocusHandled -> {
                _uiState.update { it.copy(shouldFocusVisiblePins = false) }
            }
            MapIntent.SearchAreaFocusHandled -> {
                _uiState.update { it.copy(shouldFocusSearchArea = false) }
            }
            is MapIntent.UserLocationUpdated -> {
                val currentState = _uiState.value
                val distanceMoved = if (currentState.userLatitude != null && currentState.userLongitude != null) {
                    haversineMeters(
                        currentState.userLatitude,
                        currentState.userLongitude,
                        intent.latitude,
                        intent.longitude
                    )
                } else {
                    Double.MAX_VALUE
                }

                _uiState.update {
                    it.copy(
                        userLatitude = intent.latitude,
                        userLongitude = intent.longitude,
                        locationPrecision = if (intent.isPreciseLocation) {
                            MapLocationPrecision.PRECISE
                        } else {
                            MapLocationPrecision.APPROXIMATE
                        },
                    )
                }

                // Sadece 10 metreden fazla hareket edildiyse etiketi yenile
                if (distanceMoved > 10.0) {
                    refreshAreaLabel()
                }
            }
            MapIntent.FindNearestClicked -> findNearest()
            MapIntent.RefreshActiveSession -> refreshActiveSession()
            MapIntent.ActiveSessionClicked -> openActiveSession()
            is MapIntent.VehiclePinClicked -> handleVehiclePinClicked(intent.vehicleId)
        }
    }

    private fun handleVehiclePinClicked(vehicleId: String) {
        val state = _uiState.value
        val pin = state.visiblePins.find { it.id == vehicleId }
            ?: state.vehiclePins.find { it.id == vehicleId }

        if (pin?.isInUse == true && state.activeSession?.vehicleId != vehicleId) {
            sendEffect(MapEffect.ShowError("Bu araç şu anda müsait değil."))
            return
        }

        sendEffect(
            MapEffect.NavigateToVehicleDetail(
                vehicleId = vehicleId,
                userLat = state.userLatitude,
                userLng = state.userLongitude,
            ),
        )
    }

    private fun submitLocationSearch() {
        val query = _uiState.value.searchQuery.trim()

        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    isLocationSearchActive = false,
                    searchAreaLatitude = null,
                    searchAreaLongitude = null,
                    searchAreaLabel = null,
                )
            }
            applyFilters(triggerFocus = true)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val searchArea = areaLabelResolver.resolveSearchArea(query)
            _uiState.update { it.copy(isLoading = false) }

            if (searchArea == null) {
                sendEffect(MapEffect.ShowError("Konum bulunamadı. İlçe veya mahalle adını kontrol edin."))
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLocationSearchActive = true,
                    searchAreaLatitude = searchArea.latitude,
                    searchAreaLongitude = searchArea.longitude,
                    searchAreaLabel = searchArea.areaName,
                )
            }
            applyFilters(triggerFocus = true, focusSearchArea = true)
        }
    }

    private fun loadVehicles(category: VehicleCategory = _uiState.value.selectedCategory) {
        loadVehiclesJob?.cancel()
        loadVehiclesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedCategory = category) }
            val apiType = resolveApiVehicleType(_uiState.value.selectedVehicleTypes)
            val result = fetchVehiclesFromApi(category, apiType)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { vehicles ->
                    val filteredVehicles = applyVehicleTypeFilter(
                        vehicles = vehicles,
                        selectedTypes = _uiState.value.selectedVehicleTypes,
                    )
                    val pins = filteredVehicles.map { vehicle -> vehicle.toMapPin() }
                    _uiState.update { state ->
                        val updated = state.copy(vehiclePins = pins)
                        val visiblePins = computeVisiblePins(updated)
                        updated.copy(
                            visiblePins = visiblePins,
                            nearbyCount = visiblePins.count { !it.isInUse },
                            shouldFocusVisiblePins = visiblePins.isNotEmpty(),
                            shouldFocusSearchArea = false,
                        )
                    }
                    refreshAreaLabel()
                }
                .onFailure { error ->
                    sendEffect(MapEffect.ShowError(error.message ?: "Araçlar yüklenemedi."))
                }
        }
    }

    private suspend fun fetchVehiclesFromApi(
        category: VehicleCategory,
        type: VehicleType? = null,
    ): Result<List<Vehicle>> =
        when (category) {
            VehicleCategory.ALL -> vehicleRepository.listAvailable(type = type, includeBusy = true)
            VehicleCategory.ECONOMIC -> vehicleRepository.listAvailable(
                type = type,
                segment = VehicleSegment.ECONOMY,
                includeBusy = true,
            )
            VehicleCategory.SUV -> vehicleRepository.listAvailable(
                type = type,
                segment = VehicleSegment.SUV,
                includeBusy = true,
            )
            VehicleCategory.COMFORT -> vehicleRepository.listAvailable(
                type = type,
                segment = VehicleSegment.COMFORT,
                includeBusy = true,
            )
        }

    private fun resolveApiVehicleType(types: Set<VehicleType>): VehicleType? = types.singleOrNull()

    private fun applyVehicleTypeFilter(
        vehicles: List<Vehicle>,
        selectedTypes: Set<VehicleType>,
    ): List<Vehicle> =
        if (selectedTypes.isEmpty()) {
            vehicles
        } else {
            vehicles.filter { it.type in selectedTypes }
        }

    private fun selectCategory(category: VehicleCategory) {
        loadVehicles(category)
    }

    private fun toggleVehicleTypeFilter(type: VehicleType) {
        _uiState.update { state ->
            val updatedTypes = state.selectedVehicleTypes.toMutableSet()
            if (type in updatedTypes) {
                updatedTypes.remove(type)
            } else {
                updatedTypes.add(type)
            }
            state.copy(selectedVehicleTypes = updatedTypes)
        }
        loadVehicles(_uiState.value.selectedCategory)
    }

    private fun applyFilters(triggerFocus: Boolean, focusSearchArea: Boolean = false) {
        _uiState.update { state ->
            val visiblePins = computeVisiblePins(state)
            val hasSearchCoords = state.searchAreaLatitude != null && state.searchAreaLongitude != null
            state.copy(
                visiblePins = visiblePins,
                nearbyCount = visiblePins.count { !it.isInUse },
                shouldFocusVisiblePins = triggerFocus && visiblePins.isNotEmpty(),
                shouldFocusSearchArea = focusSearchArea && visiblePins.isEmpty() && hasSearchCoords,
            )
        }
        refreshAreaLabel()
    }

    private fun computeVisiblePins(state: MapUiState): List<MapVehiclePin> {
        var pins = state.vehiclePins

        if (state.showOnlyAvailable) {
            pins = pins.filterNot { it.isInUse }
        }

        if (state.selectedVehicleTypes.isNotEmpty()) {
            pins = pins.filter { it.vehicleType in state.selectedVehicleTypes }
        }

        if (state.isLocationSearchActive) {
            val searchLat = state.searchAreaLatitude
            val searchLng = state.searchAreaLongitude
            if (searchLat != null && searchLng != null) {
                pins = pins.filter { pin ->
                    haversineMeters(searchLat, searchLng, pin.latitude, pin.longitude) <= SEARCH_RADIUS_METERS
                }
            }
        }

        return pins
    }

    private fun focusMyLocation() {
        val state = _uiState.value
        if (state.userLatitude == null || state.userLongitude == null) {
            viewModelScope.launch {
                sendEffect(MapEffect.ShowError("Konum izni verilmedi veya konum alınamadı."))
            }
            return
        }
        _uiState.update {
            it.copy(
                shouldFocusMyLocation = true,
                isLocationSearchActive = false,
                searchAreaLatitude = null,
                searchAreaLongitude = null,
                searchAreaLabel = null,
            )
        }
        applyFilters(triggerFocus = false)
        refreshAreaLabel()
    }

    private fun refreshAreaLabel() {
        val state = _uiState.value
        val reference = resolveAreaReference(state) ?: return

        refreshAreaLabelJob?.cancel()
        refreshAreaLabelJob = viewModelScope.launch {
            val pins = state.visiblePins.ifEmpty { state.vehiclePins }
            val label = areaLabelResolver.resolve(
                latitude = reference.latitude,
                longitude = reference.longitude,
                vehiclePins = pins,
                preferredAreaName = reference.preferredAreaName,
                locationPrecision = state.locationPrecision,
            )
            _uiState.update { it.copy(areaLabel = label) }
        }
    }

    private fun resolveAreaReference(state: MapUiState): AreaReference? {
        if (state.isLocationSearchActive) {
            val latitude = state.searchAreaLatitude
            val longitude = state.searchAreaLongitude
            if (latitude != null && longitude != null) {
                return AreaReference(
                    latitude = latitude,
                    longitude = longitude,
                    preferredAreaName = state.searchAreaLabel,
                )
            }
        }

        val latitude = state.userLatitude ?: return null
        val longitude = state.userLongitude ?: return null
        return AreaReference(
            latitude = latitude,
            longitude = longitude,
            preferredAreaName = null,
        )
    }

    private data class AreaReference(
        val latitude: Double,
        val longitude: Double,
        val preferredAreaName: String?,
    )

    private fun findNearest() {
        val state = _uiState.value
        val userLat = state.userLatitude
        val userLng = state.userLongitude

        android.util.Log.d("EnYakinArac", "BAŞLATILDI - Kullanici Konumu -> Lat: $userLat, Lng: $userLng")

        if (userLat == null || userLng == null) {
            sendEffect(MapEffect.ShowError("Konumunuz henüz belirlenmedi. Lütfen biraz bekleyin."))
            return
        }

        val availablePins = state.visiblePins.filter { !it.isInUse }
            .ifEmpty { state.vehiclePins.filter { !it.isInUse } }

        if (availablePins.isEmpty()) {
            sendEffect(MapEffect.ShowError("Yakınınızda müsait araç bulunamadı."))
            return
        }

        var minDistance = Float.MAX_VALUE
        var closestPin: MapVehiclePin? = null

        // Manuel döngü ile en küçük mesafeyi garantili bulma
        for (pin in availablePins) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                userLat, userLng,
                pin.latitude, pin.longitude,
                results
            )
            val calculatedDistance = results[0]
            
            android.util.Log.d(
                "EnYakinArac", 
                "Arac Plaka: ${pin.plate}, Konum: (${pin.latitude}, ${pin.longitude}) -> Mesafe: $calculatedDistance m"
            )
            
            if (calculatedDistance < minDistance) {
                minDistance = calculatedDistance
                closestPin = pin
                android.util.Log.d("EnYakinArac", "--> YENİ EN YAKIN ADAY: ${pin.plate} ($calculatedDistance m)")
            }
        }

        android.util.Log.d("EnYakinArac", "FİNAL SEÇİLEN ARAÇ: ${closestPin?.plate} ($minDistance m)")

        closestPin?.let {
            sendEffect(
                MapEffect.NavigateToVehicleDetail(
                    vehicleId = it.id,
                    userLat = userLat,
                    userLng = userLng
                )
            )
        }
    }

    private fun openActiveSession() {
        val session = _uiState.value.activeSession ?: return
        when (session.type) {
            MapActiveSessionType.RENTAL -> {
                val rentalId = session.rentalId ?: return
                sendEffect(MapEffect.NavigateToActiveRental(rentalId))
            }
            MapActiveSessionType.RESERVATION -> {
                val vehicleId = session.vehicleId ?: return
                sendEffect(MapEffect.NavigateToConfirmation(vehicleId))
            }
        }
    }

    private fun refreshActiveSession() {
        viewModelScope.launch {
            rentalRepository.getActive()
                .onSuccess { active ->
                    val vehicleName = "${active.vehicle.brand} ${active.vehicle.model}".trim()
                    val subtitle = when (active.status.uppercase()) {
                        "PREPARING" -> "Hazırlık — kilidi açıp yola çıkın"
                        else -> "Sürüş devam ediyor"
                    }
                    _uiState.update {
                        it.copy(
                            activeSession = MapActiveSession(
                                type = MapActiveSessionType.RENTAL,
                                rentalId = active.id,
                                vehicleId = active.vehicleId,
                                title = vehicleName.ifBlank { "Aktif kiralama" },
                                subtitle = subtitle,
                            ),
                        )
                    }
                    return@launch
                }

            val preparingRental = rentalRepository.listMine()
                .getOrNull()
                ?.firstOrNull { it.status == RentalStatus.PREPARING }

            if (preparingRental != null) {
                val vehicleName = listOf(preparingRental.vehicleBrand, preparingRental.vehicleModel)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                _uiState.update {
                    it.copy(
                        activeSession = MapActiveSession(
                            type = MapActiveSessionType.RENTAL,
                            rentalId = preparingRental.id,
                            vehicleId = preparingRental.vehicleId,
                            title = vehicleName.ifBlank { "Hazırlık aşaması" },
                            subtitle = "Araca gidip kilidi açın",
                        ),
                    )
                }
                return@launch
            }

            reservationRepository.getActive()
                .onSuccess { reservation ->
                    val vehicleName = "${reservation.vehicle.brand} ${reservation.vehicle.model}".trim()
                    _uiState.update {
                        it.copy(
                            activeSession = MapActiveSession(
                                type = MapActiveSessionType.RESERVATION,
                                vehicleId = reservation.vehicleId,
                                title = vehicleName.ifBlank { "Aktif rezervasyon" },
                                subtitle = "Onay ekranından kiralamayı başlatın",
                            ),
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(activeSession = null) }
                }
        }
    }

    private fun sendEffect(effect: MapEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    companion object {
        private const val SEARCH_RADIUS_METERS = 8_000.0

        private fun Vehicle.toMapPin(): MapVehiclePin =
            MapVehiclePin(
                id = id,
                priceLabel = VehiclePriceFormatter.mapPinLabel(this),
                brand = brand,
                model = model,
                plate = plate,
                category = segment.toCategory(),
                vehicleType = type,
                latitude = latitude,
                longitude = longitude,
                isInUse = status != VehicleStatus.AVAILABLE,
            )

        private fun VehicleSegment.toCategory(): VehicleCategory =
            when (this) {
                VehicleSegment.ECONOMY -> VehicleCategory.ECONOMIC
                VehicleSegment.SUV -> VehicleCategory.SUV
                VehicleSegment.COMFORT -> VehicleCategory.COMFORT
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

        private fun haversineMeters(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double,
        ): Double {
            val earthRadiusMeters = 6_371_000.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadiusMeters * c
        }
    }
}
