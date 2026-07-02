package com.turkcell.rencarapp.ui.rental.confirmation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
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
fun RentalConfirmationRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    viewModel: RentalConfirmationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RentalConfirmationEffect.NavigateBack -> onNavigateBack()
                is RentalConfirmationEffect.NavigateToSummary -> onNavigateToSummary(effect.vehicleId)
                is RentalConfirmationEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    RentalConfirmationScreen(
        state = uiState,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalConfirmationScreen(
    state: RentalConfirmationUiState,
    onIntent: (RentalConfirmationIntent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rezervasyon Onayı", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(RentalConfirmationIntent.BackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { onIntent(RentalConfirmationIntent.CompleteReservationClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text("Rezervasyonu Tamamla", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Araç Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Araç Görseli Placeholder
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "${state.vehicle?.brand ?: ""} ${state.vehicle?.model ?: ""}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${state.vehicle?.plate ?: ""} • Manuel • 5 kişi",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Yakıt %72",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Kiralama planı", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Plan Seçenekleri
            Row(modifier = Modifier.fillMaxWidth()) {
                PlanItem(
                    title = "Dakikalık",
                    price = "₺4,50/dk",
                    isSelected = state.selectedPlan == RentalPlan.MINUTELY,
                    onClick = { onIntent(RentalConfirmationIntent.PlanSelected(RentalPlan.MINUTELY)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                PlanItem(
                    title = "Saatlik",
                    price = "₺180/sa",
                    isSelected = state.selectedPlan == RentalPlan.HOURLY,
                    onClick = { onIntent(RentalConfirmationIntent.PlanSelected(RentalPlan.HOURLY)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                PlanItem(
                    title = "Günlük",
                    price = "₺1.450",
                    isSelected = state.selectedPlan == RentalPlan.DAILY,
                    onClick = { onIntent(RentalConfirmationIntent.PlanSelected(RentalPlan.DAILY)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Detaylar
            PriceDetailItem("Ücretsiz rezervasyon", state.freeReservationTime)
            PriceDetailItem("Başlangıç ücreti", state.basePrice)
            PriceDetailItem("Tahmini ücret (${state.estimatedDuration})", state.estimatedPrice, isTotal = true)

            Spacer(modifier = Modifier.height(32.dp))

            // Onay Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntent(RentalConfirmationIntent.TermsAcceptedChanged(!state.isTermsAccepted)) },
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = state.isTermsAccepted,
                    onCheckedChange = { onIntent(RentalConfirmationIntent.TermsAcceptedChanged(it)) },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1565C0))
                )
                Text(
                    text = "Kullanım şartlarını ve kasko/sigorta koşullarını okudum, onaylıyorum.",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PlanItem(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF1565C0) else Color(0xFFE0E0E0)),
        color = if (isSelected) Color(0xFFE3F2FD) else Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isSelected) Color(0xFF1565C0) else Color.Black
            )
            Text(
                text = price,
                fontSize = 12.sp,
                color = if (isSelected) Color(0xFF1565C0) else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun PriceDetailItem(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = if (isTotal) Color.Gray else Color.Gray,
            fontSize = 15.sp,
            fontWeight = if (isTotal) FontWeight.Normal else FontWeight.Normal
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTotal) 16.sp else 15.sp,
            color = if (isTotal) Color.Black else Color.Black
        )
    }
}
