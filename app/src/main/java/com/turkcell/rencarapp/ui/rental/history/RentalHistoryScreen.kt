package com.turkcell.rencarapp.ui.rental.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RentalHistoryRoute(
    viewModel: RentalHistoryViewModel = hiltViewModel(),
    onShowSnackbar: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is RentalHistoryEffect.ShowError -> onShowSnackbar(effect.message)
                is RentalHistoryEffect.ShowToast -> onShowSnackbar(effect.message)
            }
        }
    }

    RentalHistoryScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RentalHistoryScreen(
    state: RentalHistoryUiState,
    onIntent: (RentalHistoryIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding() // 🚀 EKLENDİ: Üst saat/şarj çubuğuyla çakışmayı önler
    ) {
        // --- BAŞLIK ALANI (Daha aşağıda, Premium ve Dengeli) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "KİRALAMALARIM",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black, // Daha premium bir kalınlık
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Default // 🚀 EKLENDİ: Mac/iOS uyumu
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // İstatistik Alanı (Daha şık ve okunabilir)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${state.monthlyTripCount} Yolculuk • ₺${state.monthlyTotalSpent} Toplam Harcama",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Default // 🚀 EKLENDİ
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- LİSTE ALANI ---
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 40.dp)
            ) {
                items(state.rentals) { rental ->
                    RentalHistoryCard(
                        model = rental,
                        onClick = { onIntent(RentalHistoryIntent.RentalClicked(rental.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun RentalHistoryCard(
    model: RentalUiModel,
    onClick: () -> Unit
) {
    // Ultra modern, gölgeli ve pürüzsüz kart tasarımı
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp, // Premium bir havada asılı durma hissi
            pressedElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- HARİTA / İKON ALANI (Sol Taraf - Gradient Modernizasyonu) ---
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Map,
                    contentDescription = "Harita",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // --- BİLGİ ALANI (Orta Kısım) ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = model.vehicleName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Default // 🚀 EKLENDİ
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = model.dateText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Default // 🚀 EKLENDİ
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Süre ve Mesafe Chip'leri (Hap tasarımı)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(text = model.durationText)
                    InfoChip(text = model.distanceText)
                }
            }

            // --- FİYAT VE OK ALANI (Sağ Taraf) ---
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = model.priceText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Default // 🚀 EKLENDİ
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tıklanabilir hissini artıran modern sağ ok
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Detaya Git",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Ultra Modern Chip (Hap) Tasarımı (iOS/Android Uyumlu)
@Composable
fun InfoChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = CircleShape
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default // 🚀 EKLENDİ
            ),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp) // İç boşluklar mükemmelleştirildi
        )
    }
}