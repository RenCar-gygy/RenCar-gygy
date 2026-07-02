package com.turkcell.rencarapp.ui.rental.active

import android.widget.Toast
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
    onNavigateToMain: () -> Unit,
    viewModel: ActiveRentalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActiveRentalEffect.NavigateToMain -> onNavigateToMain()
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
}

@Composable
fun ActiveRentalScreen(
    state: ActiveRentalUiState,
    onIntent: (ActiveRentalIntent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)) // Light blue background to represent map
    ) {
        // Map Top Status Bar
        Surface(
            modifier = Modifier
                .padding(top = 48.dp)
                .align(Alignment.TopCenter),
            color = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kiralama aktif - ${state.vehicleName}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Active Rental Info Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 0.dp),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Geçen süre",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = state.duration,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Current Price Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Anlık ücret", color = Color.Gray, fontSize = 11.sp)
                            Text(
                                text = state.currentPrice,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0),
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Distance Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Mesafe", color = Color.Gray, fontSize = 11.sp)
                            Text(
                                text = state.distance,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { onIntent(ActiveRentalIntent.ToggleLock) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (state.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (state.isLocked) "Kilidi Aç" else "Kilitle")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { onIntent(ActiveRentalIntent.FinishRental) },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Kiralamayı Bitir", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
