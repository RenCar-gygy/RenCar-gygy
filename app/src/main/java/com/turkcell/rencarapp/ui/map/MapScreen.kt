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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencarapp.ui.theme.RenCarAppTheme

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
private fun mapColors(darkTheme: Boolean): MapColors =
    if (darkTheme) {
        MapColors(
            mapBackground = Color(0xFF1A1F2E),
            mapBlock = Color(0xFF252B3B),
            mapRoad = Color(0xFF2F3648),
            mapPark = Color(0xFF1E3A2F),
            searchBackground = Color(0xFF111827),
            searchBorder = Color(0xFF334155),
            searchText = Color.White,
            searchPlaceholder = Color(0xFF64748B),
            filterButtonBackground = Color(0xFF111827),
            filterButtonBorder = Color(0xFF334155),
            fabBackground = Color(0xFF111827),
            fabIcon = RenCarBlue,
            sheetBackground = Color(0xFF0F172A),
            sheetTitle = Color.White,
            sheetSubtitle = Color(0xFF94A3B8),
            chipInactiveBackground = Color(0xFF1E293B),
            chipInactiveText = Color(0xFFCBD5E1),
            chipActiveBackground = RenCarBlue,
            chipActiveText = Color.White,
            inUsePin = Color(0xFF64748B),
            buttonShadow = Color(0x662563EB),
        )
    } else {
        MapColors(
            mapBackground = Color(0xFFE8EDF3),
            mapBlock = Color(0xFFF5F7FA),
            mapRoad = Color.White,
            mapPark = Color(0xFFD1FAE5),
            searchBackground = Color.White,
            searchBorder = Color(0xFFE2E8F0),
            searchText = Color(0xFF0F172A),
            searchPlaceholder = Color(0xFF94A3B8),
            filterButtonBackground = Color.White,
            filterButtonBorder = Color(0xFFE2E8F0),
            fabBackground = Color.White,
            fabIcon = RenCarBlue,
            sheetBackground = Color.White,
            sheetTitle = Color(0xFF0F172A),
            sheetSubtitle = Color(0xFF64748B),
            chipInactiveBackground = Color.White,
            chipInactiveText = Color(0xFF64748B),
            chipActiveBackground = RenCarBlue,
            chipActiveText = Color.White,
            inUsePin = Color(0xFF94A3B8),
            buttonShadow = Color(0x402563EB),
        )
    }

@Composable
fun MapRoute(
    onNavigateToVehicleDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MapEffect.NavigateToVehicleDetail -> onNavigateToVehicleDetail(effect.vehicleId)
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
    val colors = mapColors(isSystemInDarkTheme())

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        MapStubBackground(colors = colors)

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val mapWidth = maxWidth
            val mapHeight = maxHeight

            state.visiblePins.forEach { pin ->
                VehicleMapPin(
                    pin = pin,
                    colors = colors,
                    modifier = Modifier.offset(
                        x = mapWidth * pin.offsetXFraction,
                        y = mapHeight * pin.offsetYFraction,
                    ),
                    onClick = { onIntent(MapIntent.VehiclePinClicked(pin.id)) },
                )
            }
        }

        MapSearchBar(
            query = state.searchQuery,
            colors = colors,
            onQueryChange = { onIntent(MapIntent.SearchQueryChanged(it)) },
            onFilterClick = { onIntent(MapIntent.FilterClicked) },
            modifier = Modifier
                .align(Alignment.TopCenter)
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
                .clickable { onIntent(MapIntent.MyLocationClicked) },
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
private fun MapSearchBar(
    query: String,
    colors: MapColors,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                contentDescription = null,
                tint = colors.searchPlaceholder,
                modifier = Modifier.size(20.dp),
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = colors.searchText,
                    fontSize = 15.sp,
                ),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Nereden araç alacaksın?",
                            color = colors.searchPlaceholder,
                            fontSize = 15.sp,
                        )
                    }
                    inner()
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
            horizontalArrangement = Arrangement.SpaceBetween,
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.chipInactiveBackground)
                    .clickable { onIntent(MapIntent.FilterClicked) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filtre",
                    tint = colors.sheetSubtitle,
                    modifier = Modifier.size(20.dp),
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
                visiblePins = listOf(
                    MapVehiclePin("1", "₺28", VehicleCategory.ECONOMIC, 0.3f, 0.3f),
                ),
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
