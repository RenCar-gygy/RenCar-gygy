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

@Composable
fun ActiveRentalRoute(
    onNavigateToDeliveryPhotos: (String, String, String) -> Unit,
    viewModel: ActiveRentalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActiveRentalEffect.NavigateToDeliveryPhotos -> {
                    onNavigateToDeliveryPhotos(effect.rentalId, effect.vehicleName, effect.vehiclePlate)
                }
                is ActiveRentalEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
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
    Box(modifier = Modifier.fillMaxSize()) {

        // --- 1. CANLI HARİTA (MAPLIBRE) ---
        // Harita arka planı için tema rengi
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))

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
                        .background(if (state.isLocked) Color(0xFFFFA000) else Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isLocked) "Rezervasyon Aktif" else "Kiralama Aktif - ${state.vehicleName}",
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
                    text = if (state.isLocked) "Kalan Rezervasyon Süresi" else "Geçen Kullanım Süresi",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )

                // SAYACIN GÖRÜNDÜĞÜ YER
                Text(
                    text = state.duration,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = if (state.isLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                    // Kilit Butonu
                    OutlinedButton(
                        onClick = { onIntent(ActiveRentalIntent.ToggleLock) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (state.isLocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, if (state.isLocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(
                            if (state.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isLocked) "Kilidi Aç" else "Kilitle",
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
                        enabled = !state.isLocked
                    ) {
                        Text("Kiralamayı Bitir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
