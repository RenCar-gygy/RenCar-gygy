package com.turkcell.rencarapp.ui.map

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.zIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencarapp.ui.theme.RenCarAppTheme
import com.turkcell.rencarapp.data.vehicle.VehicleType
import org.maplibre.android.geometry.LatLng

private val RenCarBlue = Color(0xFF2563EB)
private val EconomicOrange = Color(0xFFF97316)
private val ComfortPurple = Color(0xFFA855F7)
private val SuvYellow = Color(0xFFEAB308)
private val TealPin = Color(0xFF14B8A6)

private data class MapColors(
    val mapBackground: Color,
    val mapBlock: Color,
    val mapRoad: Color,
    val mapPark: Color,
    val searchBackground: Color,
    val searchBorder: Color,
    val searchText: Color,
    val searchPlaceholder: Color,
    val filterButtonBackground: Color,
    val filterButtonBorder: Color,
    val fabBackground: Color,
    val fabIcon: Color,
    val sheetBackground: Color,
    val sheetTitle: Color,
    val sheetSubtitle: Color,
    val chipInactiveBackground: Color,
    val chipInactiveText: Color,
    val chipActiveBackground: Color,
    val chipActiveText: Color,
    val inUsePin: Color,
    val buttonShadow: Color,
)

@Composable
private fun mapColors(): MapColors {
    val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
    
    return remember(colorScheme) {
        MapColors(
            mapBackground = colorScheme.background,
            mapBlock = colorScheme.surfaceVariant,
            mapRoad = colorScheme.surface,
            mapPark = colorScheme.primaryContainer.copy(alpha = 0.2f),
            searchBackground = colorScheme.surface,
            searchBorder = colorScheme.outlineVariant,
            searchText = colorScheme.onSurface,
            searchPlaceholder = colorScheme.onSurfaceVariant,
            filterButtonBackground = colorScheme.surface,
            filterButtonBorder = colorScheme.outlineVariant,
            fabBackground = colorScheme.surface,
            fabIcon = colorScheme.primary,
            sheetBackground = colorScheme.surface,
            sheetTitle = colorScheme.onSurface,
            sheetSubtitle = colorScheme.onSurfaceVariant,
            chipInactiveBackground = colorScheme.surfaceVariant.copy(alpha = 0.5f),
            chipInactiveText = colorScheme.onSurfaceVariant,
            chipActiveBackground = colorScheme.primary,
            chipActiveText = colorScheme.onPrimary,
            inUsePin = colorScheme.outline,
            buttonShadow = colorScheme.primary.copy(alpha = 0.2f),
        )
    }
}

@Composable
fun MapRoute(
    onNavigateToVehicleDetail: (String, Double?, Double?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MapEffect.NavigateToVehicleDetail -> {
                    onNavigateToVehicleDetail(effect.vehicleId, effect.userLat, effect.userLng)
                }
                is MapEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    MapScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun MapScreen(
    state: MapUiState,
    onIntent: (MapIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mapColors()
    val userLocationState = rememberMapUserLocation()

    if (userLocationState.isPermissionDenied) {
        MapPermissionDeniedScreen(
            colors = colors,
            shouldOpenSettings = userLocationState.shouldOpenSettings,
            onRequestPermission = userLocationState.requestPermission,
            onOpenAppSettings = userLocationState.openAppSettings,
            modifier = modifier,
        )
        return
    }

    val myLocation = remember(state.userLatitude, state.userLongitude, userLocationState.location) {
        state.userLatitude?.let { lat ->
            state.userLongitude?.let { lng -> LatLng(lat, lng) }
        } ?: userLocationState.location
    }
    val searchFocusLocation = remember(
        state.searchAreaLatitude,
        state.searchAreaLongitude,
    ) {
        state.searchAreaLatitude?.let { lat ->
            state.searchAreaLongitude?.let { lng -> LatLng(lat, lng) }
        }
    }

    LaunchedEffect(userLocationState.location, userLocationState.isPreciseLocation) {
        userLocationState.location?.let { location ->
            onIntent(
                MapIntent.UserLocationUpdated(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    isPreciseLocation = userLocationState.isPreciseLocation,
                ),
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        MapLibreMapView(
            pins = state.visiblePins,
            onPinClick = { onIntent(MapIntent.VehiclePinClicked(it)) },
            myLocation = myLocation,
            focusMyLocation = state.shouldFocusMyLocation,
            onMyLocationFocused = { onIntent(MapIntent.MyLocationFocusHandled) },
            focusVisiblePins = state.shouldFocusVisiblePins,
            onVisiblePinsFocused = { onIntent(MapIntent.VisiblePinsFocusHandled) },
            searchFocusLocation = searchFocusLocation,
            focusSearchArea = state.shouldFocusSearchArea,
            onSearchAreaFocused = { onIntent(MapIntent.SearchAreaFocusHandled) },
            modifier = Modifier.fillMaxSize(),
        )

        if (state.isFilterSheetVisible) {
            MapFilterSheet(
                showOnlyAvailable = state.showOnlyAvailable,
                selectedVehicleTypes = state.selectedVehicleTypes,
                colors = colors,
                onShowOnlyAvailableChanged = { onIntent(MapIntent.ShowOnlyAvailableChanged(it)) },
                onVehicleTypeToggled = { onIntent(MapIntent.VehicleTypeFilterToggled(it)) },
                onDismiss = { onIntent(MapIntent.FilterSheetDismissed) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 76.dp),
            )
        }

        MapSearchBar(
            query = state.searchQuery,
            colors = colors,
            onQueryChange = { onIntent(MapIntent.SearchQueryChanged(it)) },
            onSearchSubmit = { onIntent(MapIntent.SearchSubmitted) },
            onFilterClick = { onIntent(MapIntent.FilterClicked) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 12.dp),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 250.dp)
                .size(48.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(colors.fabBackground)
                .clickable {
                    if (!userLocationState.hasPermission) {
                        userLocationState.requestPermission()
                    } else {
                        val cachedLocation = myLocation ?: userLocationState.location
                        if (cachedLocation != null) {
                            onIntent(MapIntent.MyLocationClicked)
                            onIntent(
                                MapIntent.UserLocationUpdated(
                                    latitude = cachedLocation.latitude,
                                    longitude = cachedLocation.longitude,
                                    isPreciseLocation = userLocationState.isPreciseLocation,
                                ),
                            )
                            userLocationState.refreshAndGetLocation { refreshed ->
                                if (
                                    refreshed != null &&
                                    !sameCoordinates(refreshed, cachedLocation)
                                ) {
                                    onIntent(
                                        MapIntent.UserLocationUpdated(
                                            latitude = refreshed.latitude,
                                            longitude = refreshed.longitude,
                                            isPreciseLocation = userLocationState.isPreciseLocation,
                                        ),
                                    )
                                }
                            }
                        } else {
                            userLocationState.refreshAndGetLocation { location ->
                                onIntent(MapIntent.MyLocationClicked)
                                if (location != null) {
                                    onIntent(
                                        MapIntent.UserLocationUpdated(
                                            latitude = location.latitude,
                                            longitude = location.longitude,
                                            isPreciseLocation = userLocationState.isPreciseLocation,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Konumum",
                tint = colors.fabIcon,
                modifier = Modifier.size(22.dp),
            )
        }

        MapBottomSheet(
            state = state,
            colors = colors,
            onIntent = onIntent,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun sameCoordinates(first: LatLng, second: LatLng): Boolean =
    kotlin.math.abs(first.latitude - second.latitude) < 0.0001 &&
        kotlin.math.abs(first.longitude - second.longitude) < 0.0001

@Composable
private fun MapPermissionDeniedScreen(
    colors: MapColors,
    shouldOpenSettings: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mapBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Place,
                contentDescription = null,
                tint = colors.sheetSubtitle,
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Konum izni gerekli",
                color = colors.sheetTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (shouldOpenSettings) {
                    "İzin kalıcı olarak reddedildi. Ayarlardan konum iznini manuel olarak açmanız gerekiyor."
                } else {
                    "Haritayı ve yakındaki araçları görebilmek için konum izni vermeniz gerekiyor."
                },
                color = colors.sheetSubtitle,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = if (shouldOpenSettings) onOpenAppSettings else onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = RenCarBlue),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = if (shouldOpenSettings) "Ayarlara git" else "İzni tekrar iste",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun MapStubBackground(colors: MapColors) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mapBackground),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .fillMaxSize(0.35f)
                .align(Alignment.TopStart)
                .padding(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.mapPark),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.Center)
                .background(colors.mapRoad),
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxSize(0.55f)
                .align(Alignment.CenterEnd)
                .padding(end = 80.dp)
                .background(colors.mapRoad),
        )
        repeat(6) { index ->
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 56.dp)
                    .offset(x = (index * 56).dp, y = (index * 38 + 80).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.mapBlock),
            )
        }
    }
}

@Composable
private fun MapFilterSheet(
    showOnlyAvailable: Boolean,
    selectedVehicleTypes: Set<VehicleType>,
    colors: MapColors,
    onShowOnlyAvailableChanged: (Boolean) -> Unit,
    onVehicleTypeToggled: (VehicleType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.searchBackground)
            .border(1.dp, colors.searchBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Filtreler",
                color = colors.searchText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Kapat",
                color = RenCarBlue,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onDismiss),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Araç tipi",
            color = colors.searchText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "Birden fazla tip seçebilirsiniz. Seçim yoksa tüm tipler gösterilir.",
            color = colors.searchPlaceholder,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VehicleType.entries.forEach { type ->
                FilterTypeChip(
                    label = type.displayLabel(),
                    isSelected = type in selectedVehicleTypes,
                    colors = colors,
                    onClick = { onVehicleTypeToggled(type) },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Yalnızca müsait araçlar",
                    color = colors.searchText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Kullanımdaki araçları haritadan gizler",
                    color = colors.searchPlaceholder,
                    fontSize = 12.sp,
                )
            }
            Switch(
                checked = showOnlyAvailable,
                onCheckedChange = onShowOnlyAvailableChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = RenCarBlue,
                ),
            )
        }
    }
}

@Composable
private fun FilterTypeChip(
    label: String,
    isSelected: Boolean,
    colors: MapColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (isSelected) colors.chipActiveBackground else colors.chipInactiveBackground
    val textColor = if (isSelected) colors.chipActiveText else colors.chipInactiveText
    val borderColor = if (isSelected) colors.chipActiveBackground else colors.searchBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

private fun VehicleType.displayLabel(): String =
    when (this) {
        VehicleType.SEDAN -> "Sedan"
        VehicleType.HATCHBACK -> "Hatchback"
        VehicleType.SUV -> "SUV"
        VehicleType.STATION -> "Station"
        VehicleType.MINIVAN -> "Minivan"
    }

@Composable
private fun MapSearchBar(
    query: String,
    colors: MapColors,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(colors.searchBackground)
                .border(1.dp, colors.searchBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ara",
                tint = colors.searchPlaceholder,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        keyboardController?.hide()
                        onSearchSubmit()
                    },
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = TextStyle(
                    color = colors.searchText,
                    fontSize = 15.sp,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = true,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        onSearchSubmit()
                    },
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Nereden araç alacaksın?",
                                color = colors.searchPlaceholder,
                                fontSize = 15.sp,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }

        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(colors.filterButtonBackground)
                .border(1.dp, colors.filterButtonBorder, RoundedCornerShape(14.dp))
                .clickable(onClick = onFilterClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filtre",
                tint = colors.searchText,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun VehicleMapPin(
    pin: MapVehiclePin,
    colors: MapColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val pinColor = when {
        pin.isInUse -> colors.inUsePin
        pin.category == VehicleCategory.ECONOMIC -> EconomicOrange
        pin.category == VehicleCategory.COMFORT -> ComfortPurple
        pin.category == VehicleCategory.SUV -> SuvYellow
        else -> TealPin
    }

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(pinColor)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!pin.isInUse) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = pin.priceLabel,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                .background(pinColor),
        )
    }
}

@Composable
private fun MapBottomSheet(
    state: MapUiState,
    colors: MapColors,
    onIntent: (MapIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(colors.sheetBackground)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Yakınında ${state.nearbyCount} araç",
                    color = colors.sheetTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.areaLabel,
                    color = colors.sheetSubtitle,
                    fontSize = 13.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryChip(
                label = "Tümü",
                isSelected = state.selectedCategory == VehicleCategory.ALL,
                dotColor = null,
                colors = colors,
                onClick = { onIntent(MapIntent.CategorySelected(VehicleCategory.ALL)) },
            )
            CategoryChip(
                label = "Ekonomik",
                isSelected = state.selectedCategory == VehicleCategory.ECONOMIC,
                dotColor = EconomicOrange,
                colors = colors,
                onClick = { onIntent(MapIntent.CategorySelected(VehicleCategory.ECONOMIC)) },
            )
            CategoryChip(
                label = "Konfor",
                isSelected = state.selectedCategory == VehicleCategory.COMFORT,
                dotColor = ComfortPurple,
                colors = colors,
                onClick = { onIntent(MapIntent.CategorySelected(VehicleCategory.COMFORT)) },
            )
            CategoryChip(
                label = "SUV",
                isSelected = state.selectedCategory == VehicleCategory.SUV,
                dotColor = SuvYellow,
                colors = colors,
                onClick = { onIntent(MapIntent.CategorySelected(VehicleCategory.SUV)) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onIntent(MapIntent.FindNearestClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = colors.buttonShadow),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RenCarBlue,
                contentColor = Color.White,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Place,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "En Yakın Aracı Bul",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    dotColor: Color?,
    colors: MapColors,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) colors.chipActiveBackground else colors.chipInactiveBackground,
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = colors.searchBorder,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        dotColor?.let { color ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
            )
        }
        Text(
            text = label,
            color = if (isSelected) colors.chipActiveText else colors.chipInactiveText,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Preview(showBackground = true, name = "Map Light")
@Composable
private fun MapScreenLightPreview() {
    RenCarAppTheme(darkTheme = false, dynamicColor = false) {
        MapScreen(
            state = MapUiState(
                nearbyCount = 0,
                areaLabel = "Üsküdar çevresinde · 3 dk uzaklıkta",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, name = "Map Dark")
@Composable
private fun MapScreenDarkPreview() {
    RenCarAppTheme(darkTheme = true, dynamicColor = false) {
        MapScreen(state = MapUiState(), onIntent = {})
    }
}
