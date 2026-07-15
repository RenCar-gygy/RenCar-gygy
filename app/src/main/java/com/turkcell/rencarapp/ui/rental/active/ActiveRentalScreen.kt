package com.turkcell.rencarapp.ui.rental.active

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ActiveRentalScreen(
    state: ActiveRentalUiState,
    onIntent: (ActiveRentalIntent) -> Unit
) {
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

    Box(modifier = Modifier.fillMaxSize()) {

        if (vehiclePins.isNotEmpty() || state.isVehicleLocationPending) {
            MapLibreMapView(
                pins = vehiclePins,
                onPinClick = {},
                myLocation = vehicleLocation,
                followLocationWithPan = canFollowVehicle,
                pinFocusZoom = 15.0,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }

        if (state.isVehicleLocationPending) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp
            ) {
                Text(
                    text = "Araç konumu alınıyor…",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // --- 2. ÜST BİLGİ BARI ---
        Surface(
            modifier = Modifier
                .padding(top = 48.dp)
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                state.isReservationActive -> Color(0xFFFFA000)
                                state.isPreparingRental -> Color(0xFF2196F3)
                                else -> Color(0xFF4CAF50)
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        state.isReservationActive -> "Rezervasyon Aktif"
                        state.isPreparingRental -> "Kiralama Hazırlanıyor - ${state.vehicleName}"
                        else -> "Kiralama Aktif - ${state.vehicleName}"
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- 3. ALT BİLGİ VE KONTROL KARTI ---
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sürükleme Çubuğu (Handle)
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = when {
                        state.isReservationActive -> "Kalan Rezervasyon Süresi"
                        state.isPreparingRental -> "Başlamak için kilidi açın"
                        else -> "Geçen Kullanım Süresi"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )

                // SAYACIN GÖRÜNDÜĞÜ YER
                Text(
                    text = state.duration,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = if (state.isReservationActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )

                if (state.isReservationActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Süre dolmadan kiralamayı başlatın",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (state.canCancelRental) {
                    OutlinedButton(
                        onClick = { onIntent(ActiveRentalIntent.CancelRentalClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("Kiralamadan Vazgeç", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Fiyat Kartı
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Anlık ücret", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = state.currentPrice,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Mesafe Kartı
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Mesafe", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = state.distance,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Kilit Butonu (Yalnızca kiralama aktifse veya rezervasyondan kiralama başlatılacaksa anlamlı)
                    // Ancak v2'de rezervasyon -> kiralama (PREPARING) -> start (ACTIVE) akışı var.
                    // Bu ekran "Kiralama başladıktan sonraki" ekran ise, isReservationActive false olmalı.
                    
                    OutlinedButton(
                        onClick = { onIntent(ActiveRentalIntent.ToggleLock) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (state.isLocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, if (state.isLocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline),
                        enabled = !state.isReservationActive
                    ) {
                        Icon(
                            if (state.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                state.isPreparingRental && state.isLocked -> "Kilidi Aç ve Başlat"
                                state.isLocked -> "Kilidi Aç"
                                else -> "Kilitle"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Kiralamayı Bitir Butonu
                    Button(
                        onClick = { onIntent(ActiveRentalIntent.FinishRental) },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        enabled = !state.isLocked && !state.isReservationActive && !state.isPreparingRental
                    ) {
                        Text("Kiralamayı Bitir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
