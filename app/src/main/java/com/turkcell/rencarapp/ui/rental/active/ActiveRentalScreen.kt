package com.turkcell.rencarapp.ui.rental.active

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencarapp.data.vehicle.VehicleType
import com.turkcell.rencarapp.ui.map.MapLibreMapView
import com.turkcell.rencarapp.ui.map.MapVehiclePin
import com.turkcell.rencarapp.ui.map.VehicleCategory
import org.maplibre.android.geometry.LatLng

private val RenCarBlue = Color(0xFF2563EB)
private val RenCarBlueGlow = Color(0x662563EB)
private val SuccessGreen = Color(0xFF22C55E)
private val WarningOrange = Color(0xFFF59E0B)
private val ErrorRed = Color(0xFFEF4444)

@Composable
fun ActiveRentalRoute(
    onNavigateToStartPhotos: (String, String, String) -> Unit,
    onNavigateToSummary: (String) -> Unit,
    onNavigateBackAfterCancel: () -> Unit,
    viewModel: ActiveRentalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActiveRentalEffect.NavigateToStartPhotos -> {
                    onNavigateToStartPhotos(effect.rentalId, effect.vehicleName, effect.vehiclePlate)
                }
                is ActiveRentalEffect.NavigateToSummary -> {
                    onNavigateToSummary(effect.rentalId)
                }
                is ActiveRentalEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is ActiveRentalEffect.NavigateBackAfterCancel -> onNavigateBackAfterCancel()
            }
        }
    }

    ActiveRentalScreen(
        state = uiState,
        onIntent = viewModel::onIntent
    )

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = RenCarBlue)
        }
    }
}

@Composable
fun ActiveRentalScreen(
    state: ActiveRentalUiState,
    onIntent: (ActiveRentalIntent) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surfaceColor = if (isDark) Color(0xFF111827) else Color.White
    val backgroundColor = if (isDark) Color(0xFF0B0F14) else Color(0xFFF8FAFC)

    val vehiclePins = remember(
        state.rentalId,
        state.vehicleName,
        state.vehiclePlate,
        state.vehicleLatitude,
        state.vehicleLongitude,
    ) {
        val latitude = state.vehicleLatitude
        val longitude = state.vehicleLongitude
        if (latitude != null && longitude != null) {
            listOf(
                MapVehiclePin(
                    id = state.rentalId,
                    priceLabel = state.vehiclePlate,
                    brand = state.vehicleName,
                    model = "",
                    plate = state.vehiclePlate,
                    category = VehicleCategory.ALL,
                    vehicleType = VehicleType.SEDAN,
                    latitude = latitude,
                    longitude = longitude,
                    isInUse = true,
                ),
            )
        } else {
            emptyList()
        }
    }

    val vehicleLocation = remember(state.vehicleLatitude, state.vehicleLongitude) {
        state.vehicleLatitude?.let { latitude ->
            state.vehicleLongitude?.let { longitude -> LatLng(latitude, longitude) }
        }
    }
    val canFollowVehicle = !state.isReservationActive && vehicleLocation != null

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {

        if (vehiclePins.isNotEmpty() || state.isVehicleLocationPending) {
            MapLibreMapView(
                pins = vehiclePins,
                onPinClick = {},
                myLocation = vehicleLocation,
                followLocationWithPan = canFollowVehicle,
                pinFocusZoom = 15.0,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (state.isVehicleLocationPending) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 100.dp),
                color = surfaceColor.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = RenCarBlue)
                    Text(
                        text = "Araç konumu güncelleniyor…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- 2. ÜST BİLGİ BARI ---
        Surface(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 16.dp)
                .align(Alignment.TopCenter)
                .shadow(8.dp, CircleShape),
            color = surfaceColor,
            shape = CircleShape,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                state.isReservationActive -> WarningOrange
                                state.isPreparingRental -> RenCarBlue
                                else -> SuccessGreen
                            }
                        )
                )
                Text(
                    text = when {
                        state.isReservationActive -> "Rezervasyon Aktif"
                        state.isPreparingRental -> "Sürüş Hazırlığı"
                        else -> "Kiralama Aktif"
                    },
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                VerticalDivider(modifier = Modifier.height(16.dp), color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                Text(
                    text = state.vehiclePlate,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // --- 3. ALT BİLGİ VE KONTROL KARTI ---
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sürükleme Çubuğu
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when {
                            state.isReservationActive -> "Kalan Rezervasyon Süresi"
                            state.isPreparingRental -> "Aracın kilidini açın"
                            else -> "Sürüş Süresi"
                        },
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = state.duration,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = when {
                            state.isReservationActive -> WarningOrange
                            state.isPreparingRental -> RenCarBlue
                            else -> RenCarBlue
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Ücret Kartı
                    StatItem(
                        icon = Icons.Outlined.Timer,
                        label = "Anlık Ücret",
                        value = state.currentPrice,
                        modifier = Modifier.weight(1f),
                        isDark = isDark,
                        valueColor = RenCarBlue
                    )

                    // Mesafe Kartı
                    StatItem(
                        icon = Icons.Outlined.Straighten,
                        label = "Mesafe",
                        value = state.distance,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Kilit Kontrolü
                    Button(
                        onClick = { onIntent(ActiveRentalIntent.ToggleLock) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(if (!state.isLocked) 8.dp else 0.dp, RoundedCornerShape(16.dp), spotColor = RenCarBlueGlow),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isLocked) RenCarBlue else if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9),
                            contentColor = if (state.isLocked) Color.White else (if (isDark) Color.White else Color(0xFF0F172A))
                        ),
                        enabled = !state.isReservationActive
                    ) {
                        Icon(
                            if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isLocked) "Kilidi Aç" else "Kilitle",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Bitir Butonu
                    Button(
                        onClick = { onIntent(ActiveRentalIntent.FinishRental) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!state.isLocked && !state.isPreparingRental) ErrorRed else (if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9)).copy(alpha = 0.5f),
                            contentColor = if (!state.isLocked && !state.isPreparingRental) Color.White else (if (isDark) Color(0xFF475569) else Color(0xFF94A3B8))
                        ),
                        enabled = !state.isLocked && !state.isReservationActive && !state.isPreparingRental
                    ) {
                        Text("Sürüşü Bitir", fontWeight = FontWeight.Bold)
                    }
                }

                if (state.canCancelRental) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { onIntent(ActiveRentalIntent.CancelRentalClicked) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Rezervasyonu İptal Et", 
                            color = ErrorRed, 
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isDark: Boolean,
    valueColor: Color? = null
) {
    Surface(
        modifier = modifier,
        color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                Text(
                    text = label, 
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), 
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                color = valueColor ?: (if (isDark) Color.White else Color(0xFF0F172A)),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
