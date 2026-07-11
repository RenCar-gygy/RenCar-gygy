package com.turkcell.rencarapp.ui.vehicle.detail

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkcell.rencarapp.data.vehicle.VehiclePriceFormatter
import com.turkcell.rencarapp.ui.map.MapLibreMapView
import com.turkcell.rencarapp.ui.map.MapVehiclePin
import com.turkcell.rencarapp.ui.map.VehicleCategory
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun VehicleDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToConfirmation: (String) -> Unit,
    viewModel: VehicleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        viewModel.onIntent(VehicleDetailIntent.BackClicked)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VehicleDetailEffect.NavigateBack -> onNavigateBack()
                is VehicleDetailEffect.NavigateToConfirmation -> onNavigateToConfirmation(effect.vehicleId)
                is VehicleDetailEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    VehicleDetailScreen(
        state = uiState,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun VehicleDetailScreen(
    state: VehicleDetailUiState,
    onIntent: (VehicleDetailIntent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.vehicle != null) {
            val pin = remember(state.vehicle) {
                MapVehiclePin(
                    id = state.vehicle.id,
                    priceLabel = VehiclePriceFormatter.mapPinLabel(state.vehicle.pricePerDay),
                    brand = state.vehicle.brand,
                    model = state.vehicle.model,
                    plate = state.vehicle.plate,
                    category = VehicleCategory.ALL,
                    vehicleType = state.vehicle.type,
                    latitude = state.vehicle.latitude,
                    longitude = state.vehicle.longitude,
                    isInUse = false
                )
            }

            MapLibreMapView(
                pins = listOf(pin),
                onPinClick = {},
                gesturesEnabled = false,
                pinFocusZoom = 15.0,
                focusVisiblePins = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .align(Alignment.TopCenter)
            )
        }

        // Ana Kart ve İçerik (Z-index: 1)
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.error != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = state.error, color = MaterialTheme.colorScheme.error)
                Button(onClick = { onIntent(VehicleDetailIntent.LoadVehicle) }) {
                    Text("Tekrar Dene")
                }
            }
        } else if (state.vehicle != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Başlık
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${state.vehicle.brand} ${state.vehicle.model}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${state.vehicle.plate} • ${state.distanceLabel}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = state.vehicle.status.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Görsel Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detay Grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoItem(
                        icon = Icons.Default.LocalGasStation,
                        label = "Yakıt",
                        value = state.fuelLevel,
                        subValue = "Dolu depo",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    InfoItem(
                        icon = Icons.Default.Speed,
                        label = "Menzil",
                        value = state.range,
                        subValue = "Dolu depo",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoItem(
                        icon = Icons.Default.Settings,
                        label = "Vites",
                        value = state.transmission,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    InfoItem(
                        icon = Icons.Default.Group,
                        label = "Koltuk",
                        value = state.seatingCapacity,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Fiyat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = VehiclePriceFormatter.minutelyLabel(state.vehicle.pricePerDay),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = VehiclePriceFormatter.hourlyLabel(state.vehicle.pricePerDay),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Aksiyon Butonları
                Button(
                    onClick = { onIntent(VehicleDetailIntent.ReserveClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Rezerve Et", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        }

        // Geri Butonu (Z-index: 2 - En üstte olması için sonda tanımlandı)
        IconButton(
            onClick = { onIntent(VehicleDetailIntent.BackClicked) },
            modifier = Modifier
                .statusBarsPadding() // Kameranın/Durum çubuğunun altında kalmasını önler
                .padding(16.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, 
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = value, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subValue != null) {
                    Text(
                        text = subValue, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), 
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
