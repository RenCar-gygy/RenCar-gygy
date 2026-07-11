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
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { onIntent(RentalSummaryIntent.PayClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Kiralamayı Bitir ve Öde",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
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
                    color = MaterialTheme.colorScheme.primary, // Tema ana rengi
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Text(
                            text = "Yolculuk Özeti",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF4CAF50), // Başarı yeşili sabit kalabilir veya onPrimaryContainer
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Araç başarıyla teslim edildi",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
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
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Buraya araç görseli gelecek, şimdilik placeholder
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🚙", fontSize = 48.sp)
                        Text(
                            text = "ÖDEME DETAYI", 
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tamamen API'ye bağlı araç bilgisi
            Text(
                text = uiState.vehicleName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = uiState.plate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tamamen API'ye bağlı fatura özeti
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    FeeRow("Kiralama Süresi", uiState.durationText)
                    FeeRow("Katedilen Mesafe", uiState.distanceText)

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    FeeRow("Kiralama Ücreti", uiState.rentalFee)
                    FeeRow("Hizmet Bedeli", uiState.serviceFee)

                    if (uiState.discount.isNotEmpty()) {
                        FeeRow("İndirim", uiState.discount, isDiscount = true)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toplam Ödenecek",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.totalFee,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Ödeme Yöntemi Kartı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp, 32.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            uiState.cardBrand, 
                            color = MaterialTheme.colorScheme.onPrimary, 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Mastercard •••• ${uiState.cardLast4}", 
                            fontWeight = FontWeight.SemiBold, 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Varsayılan Kart", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = { onIntent(RentalSummaryIntent.ChangeCardClicked) }) {
                    Text(
                        text = "Değiştir", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Bold
                    )
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
        Text(
            text = title, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isDiscount) FontWeight.Bold else FontWeight.Medium,
            color = if (isDiscount) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
    }
}
