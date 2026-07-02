package com.turkcell.rencarapp.ui.vehicle.detail

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun VehicleDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToConfirmation: (String) -> Unit,
    viewModel: VehicleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
            .background(Color(0xFFF5F5F5))
    ) {
        // Geri Butonu
        IconButton(
            onClick = { onIntent(VehicleDetailIntent.BackClicked) },
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
        }

        // Ana Kart
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(top = 100.dp),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                            text = "${state.vehicle?.brand ?: ""} ${state.vehicle?.model ?: ""}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.vehicle?.plate ?: ""} • 250 m uzaklıkta",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = state.vehicle?.type ?: "MÜSAİT",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
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
                        tint = Color.LightGray
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
                            text = "₺4,50 /dk",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Saatlik ₺${state.vehicle?.pricePerDay?.toInt() ?: 180}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Aksiyon Butonları
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { onIntent(VehicleDetailIntent.ReserveClicked) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Rezerve Et")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { onIntent(VehicleDetailIntent.UnlockClicked) },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kilidi Aç")
                    }
                }
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF9F9F9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, color = Color.Gray, fontSize = 11.sp)
                Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (subValue != null) {
                    Text(text = subValue, color = Color.LightGray, fontSize = 10.sp)
                }
            }
        }
    }
}
