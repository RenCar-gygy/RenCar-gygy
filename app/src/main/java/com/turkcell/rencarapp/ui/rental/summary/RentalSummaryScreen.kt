package com.turkcell.rencarapp.ui.rental.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RentalSummaryRoute(
    viewModel: RentalSummaryViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RentalSummaryEffect.NavigateToHome -> onNavigateToHome()
                is RentalSummaryEffect.ShowError -> onShowSnackbar(effect.message)
                is RentalSummaryEffect.ShowToast -> onShowSnackbar(effect.message)
            }
        }
    }

    RentalSummaryScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun RentalSummaryScreen(
    uiState: RentalSummaryUiState,
    onIntent: (RentalSummaryIntent) -> Unit
) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = { onIntent(RentalSummaryIntent.PayClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E63D8))
            ) {
                Text(
                    text = "Kiralamayı Bitir ve Öde",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üstteki Koyu Kavisli Alan ve Araç Görseli
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    color = Color(0xFF152238),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Text(
                            text = "Yolculuk Özeti",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Araç başarıyla teslim edildi",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(140.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🚙 ÖDEME DETAYI", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tamamen API'ye bağlı araç bilgisi
            Text(
                text = uiState.vehicleName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = uiState.plate,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tamamen API'ye bağlı fatura özeti
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Fatura Detayı",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    FeeRow("Kiralama Süresi", uiState.durationText)
                    FeeRow("Katedilen Mesafe", uiState.distanceText)

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    FeeRow("Kiralama Ücreti", uiState.rentalFee)
                    FeeRow("Hizmet Bedeli", uiState.serviceFee)

                    if (uiState.discount.isNotEmpty()) {
                        FeeRow("İndirim", uiState.discount, isDiscount = true)
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toplam Ödenecek",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = uiState.totalFee,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFF1E63D8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Ödeme Yöntemi Kartı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp, 32.dp)
                            .background(Color(0xFF152238), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.cardBrand, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Mastercard •••• ${uiState.cardLast4}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = "Varsayılan Kart", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                TextButton(onClick = { onIntent(RentalSummaryIntent.ChangeCardClicked) }) {
                    Text(text = "Değiştir", color = Color(0xFF1E63D8), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FeeRow(title: String, amount: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 14.sp, color = Color.Gray)
        Text(
            text = amount,
            fontSize = 14.sp,
            fontWeight = if (isDiscount) FontWeight.Bold else FontWeight.Medium,
            color = if (isDiscount) Color(0xFF4CAF50) else Color.Black
        )
    }
}