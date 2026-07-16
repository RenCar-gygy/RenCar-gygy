package com.turkcell.rencarapp.ui.vehicle.detail

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkcell.rencarapp.data.rental.RentalPlan
import com.turkcell.rencarapp.data.rental.defaultQuoteMinutes
import com.turkcell.rencarapp.data.vehicle.VehiclePriceFormatter
import com.turkcell.rencarapp.ui.map.MapLibreMapView
import com.turkcell.rencarapp.ui.rental.confirmation.PlanItem
import com.turkcell.rencarapp.ui.map.MapVehiclePin
import com.turkcell.rencarapp.ui.map.VehicleCategory
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val RenCarBlue = Color(0xFF2563EB)
private val RenCarBlueGlow = Color(0x662563EB)

@Composable
fun VehicleDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToConfirmation: (String, com.turkcell.rencarapp.data.rental.RentalPlan) -> Unit,
    onNavigateToActiveRental: (String) -> Unit,
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
                is VehicleDetailEffect.NavigateToConfirmation -> onNavigateToConfirmation(effect.vehicleId, effect.plan)
                is VehicleDetailEffect.NavigateToActiveRental -> onNavigateToActiveRental(effect.rentalId)
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
    val isDark = isSystemInDarkTheme()
    val surfaceColor = if (isDark) Color(0xFF111827) else Color.White
    val backgroundColor = if (isDark) Color(0xFF0B0F14) else Color(0xFFF8FAFC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (state.vehicle != null) {
            val pin = remember(state.vehicle) {
                MapVehiclePin(
                    id = state.vehicle.id,
                    priceLabel = VehiclePriceFormatter.mapPinLabel(state.vehicle),
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
                    .height(380.dp)
                    .align(Alignment.TopCenter)
            )

            // Harita üzerine degrade geçiş
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                backgroundColor.copy(alpha = 0.5f),
                                backgroundColor
                            ),
                            startY = 200f
                        )
                    )
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = RenCarBlue
            )
        } else if (state.error != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = state.error, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onIntent(VehicleDetailIntent.LoadVehicle) },
                    colors = ButtonDefaults.buttonColors(containerColor = RenCarBlue)
                ) {
                    Text("Tekrar Dene")
                }
            }
        } else if (state.vehicle != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(300.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Handle bar
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Başlık ve Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.vehicle.brand,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = RenCarBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = state.vehicle.model,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.PinDrop,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${state.vehicle.plate} • ${state.distanceLabel}",
                                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Surface(
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isDark) null else BorderStroke(1.dp, Color(0xFFDBEAFE))
                            ) {
                                Text(
                                    text = state.vehicle.status.name,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = RenCarBlue,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Araç Özellikleri Grid
                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoItem(
                                icon = Icons.Outlined.LocalGasStation,
                                label = "Yakıt",
                                value = "%${state.vehicle.fuelPercent}",
                                subValue = "${state.vehicle.rangeKm} km menzil",
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            InfoItem(
                                icon = Icons.Outlined.Speed,
                                label = "Segment",
                                value = state.vehicle.segment.name,
                                subValue = state.vehicle.type.name,
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoItem(
                                icon = Icons.Outlined.Settings,
                                label = "Şanzıman",
                                value = if (state.vehicle.transmission.name == "MANUAL") "Manuel" else "Otomatik",
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            InfoItem(
                                icon = Icons.Outlined.Group,
                                label = "Kapasite",
                                value = "${state.vehicle.seats} Koltuk",
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Kiralama planı",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            PlanItem(
                                title = "Dakikalık",
                                price = VehiclePriceFormatter.planPriceLabel(state.vehicle, RentalPlan.PER_MINUTE),
                                isSelected = state.selectedPlan == RentalPlan.PER_MINUTE,
                                onClick = {
                                    onIntent(
                                        VehicleDetailIntent.PlanChanged(
                                            RentalPlan.PER_MINUTE,
                                            RentalPlan.PER_MINUTE.defaultQuoteMinutes(),
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                isDark = isDark,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PlanItem(
                                title = "Saatlik",
                                price = VehiclePriceFormatter.planPriceLabel(state.vehicle, RentalPlan.HOURLY),
                                isSelected = state.selectedPlan == RentalPlan.HOURLY,
                                onClick = {
                                    onIntent(
                                        VehicleDetailIntent.PlanChanged(
                                            RentalPlan.HOURLY,
                                            RentalPlan.HOURLY.defaultQuoteMinutes(),
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                isDark = isDark,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PlanItem(
                                title = "Günlük",
                                price = VehiclePriceFormatter.planPriceLabel(state.vehicle, RentalPlan.DAILY),
                                isSelected = state.selectedPlan == RentalPlan.DAILY,
                                onClick = {
                                    onIntent(
                                        VehicleDetailIntent.PlanChanged(
                                            RentalPlan.DAILY,
                                            RentalPlan.DAILY.defaultQuoteMinutes(),
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                isDark = isDark,
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Fiyat ve Rezervasyon Paneli
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Kiralama Ücreti",
                                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = VehiclePriceFormatter.planPriceAmount(
                                                state.vehicle,
                                                state.selectedPlan,
                                            ),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RenCarBlue
                                        )
                                        Text(
                                            text = VehiclePriceFormatter.planUnitSuffix(state.selectedPlan),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                                        )
                                    }
                                    if (!state.isReservable && state.unavailableMessage != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = state.unavailableMessage,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFE65100),
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onIntent(VehicleDetailIntent.ReserveClicked) },
                                    enabled = !state.isReserving && state.isReservable,
                                    modifier = Modifier
                                        .height(52.dp)
                                        .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = RenCarBlueGlow),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RenCarBlue,
                                        contentColor = Color.White,
                                        disabledContainerColor = RenCarBlue.copy(alpha = 0.5f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 24.dp)
                                ) {
                                    if (state.isReserving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Rezerve Et", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // Üst Bar - Geri Butonu
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { onIntent(VehicleDetailIntent.BackClicked) },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF1F2937).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f))
                    .shadow(if (isDark) 0.dp else 4.dp, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = "Geri",
                    tint = if (isDark) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String? = null,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Surface(
        modifier = modifier,
        color = if (isDark) Color(0xFF1E293B) else Color.White,
        shape = RoundedCornerShape(16.dp),
        border = if (isDark) BorderStroke(1.dp, Color(0xFF334155)) else BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RenCarBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label, 
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), 
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = value, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                if (subValue != null) {
                    Text(
                        text = subValue, 
                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), 
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
