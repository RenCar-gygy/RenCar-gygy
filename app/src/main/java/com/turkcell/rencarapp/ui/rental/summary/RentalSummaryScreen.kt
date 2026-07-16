package com.turkcell.rencarapp.ui.rental.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.turkcell.rencarapp.data.rental.PaymentMethod

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

    // YENİ EKLENDİ: Eğer veriler henüz yükleniyorsa, boş/eski ekran yerine yükleniyor ikonu göster.
    // (ViewModel'da fetch işlemi başladığında isLoading'i true yapmayı unutmayın)
    if (uiState.isLoading || uiState.vehicleName.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        RentalSummaryScreen(
            uiState = uiState,
            onIntent = viewModel::onIntent
        )
    }

    if (uiState.showPaymentMethodSheet) {
        PaymentMethodBottomSheet(
            uiState = uiState,
            onIntent = viewModel::onIntent,
        )
    }
}

@Composable
fun RentalSummaryScreen(
    uiState: RentalSummaryUiState,
    onIntent: (RentalSummaryIntent) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 24.dp,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    if (!uiState.isPaid) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ödeme yöntemi",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = uiState.paymentMethodLabel.ifBlank { "Seçilmedi" },
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Text(
                                text = "Değiştir",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onIntent(RentalSummaryIntent.ChangePaymentMethodClicked) }
                                    .padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { onIntent(RentalSummaryIntent.PayClicked) },
                            enabled = !uiState.isPaying && !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            if (uiState.isPaying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    text = "Kiralamayı Bitir ve Öde",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Bu yolculuk ödendi.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E7D32),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onIntent(RentalSummaryIntent.NavigateHomeClicked) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text("Ana Sayfaya Dön")
                        }
                    }
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
            // --- ÜST BAŞLIK VE KESİŞEN ARAÇ KARTI ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                    ) {
                        Text(
                            text = "Yolculuk Özeti",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Default
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Araç başarıyla teslim edildi",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Default
                                )
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(110.dp)
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🚙", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = uiState.vehicleName, // VERİ BURADAN GELİYOR
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Default
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.plate, // VERİ BURADAN GELİYOR
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Default
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- FATURA DETAYI KARTI ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Fatura Detayı",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Default
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                    FeeRow("Kiralama Süresi", uiState.durationText)
                    FeeRow("Katedilen Mesafe", uiState.distanceText)

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                    FeeRow("Kullanım Ücreti", uiState.rentalFee)
                    if (uiState.startFee.isNotEmpty()) {
                        FeeRow("Açılış Ücreti", uiState.startFee)
                    }
                    FeeRow("Hizmet Bedeli", uiState.serviceFee)

                    if (!uiState.isPaid) {
                        OutlinedTextField(
                            value = uiState.discountCodeInput,
                            onValueChange = { onIntent(RentalSummaryIntent.DiscountCodeChanged(it)) },
                            label = { Text("İndirim kodu (opsiyonel)") },
                            placeholder = { Text("Örn. İLKSÜRÜŞ") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isPaying,
                        )
                    }

                    if (uiState.discount.isNotEmpty()) {
                        FeeRow("İndirim Kampanyası", uiState.discount, isDiscount = true)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                    // TOPLAM TUTAR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toplam Ödenecek",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.totalFee, // VERİ BURADAN GELİYOR
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Default
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodBottomSheet(
    uiState: RentalSummaryUiState,
    onIntent: (RentalSummaryIntent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onIntent(RentalSummaryIntent.DismissPaymentMethodSheet) },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Ödeme yöntemi seç",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntent(RentalSummaryIntent.SelectWalletPayment) },
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.selectedMethod == PaymentMethod.WALLET) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cüzdan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    uiState.walletBalance?.let {
                        Text("Bakiye: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    val balance = uiState.walletBalanceAmount
                    if (balance != null && balance < uiState.payableAmount && uiState.payableAmount > 0) {
                        Text(
                            text = "Bakiye yetersiz — önce bakiye yükleyin veya kart seçin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.availableCards) { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIntent(RentalSummaryIntent.SelectCardPayment(card.id)) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (
                                uiState.selectedMethod == PaymentMethod.CARD &&
                                uiState.selectedCardId == card.id
                            ) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${card.brand} •••• ${card.last4}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            )
                            if (card.isDefault) {
                                Text("Varsayılan kart", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            if (uiState.availableCards.isEmpty()) {
                Text(
                    text = "Kayıtlı kart yok. Cüzdan sekmesinden kart ekleyebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun FeeRow(title: String, amount: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Default
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isDiscount) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontFamily = FontFamily.Default
            ),
            color = if (isDiscount) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
    }
}