package com.turkcell.rencarapp.ui.rental.confirmation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencarapp.data.rental.RentalPlan
import com.turkcell.rencarapp.data.rental.requiresScheduledEndDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RenCarBlue = Color(0xFF2563EB)
private val RenCarBlueGlow = Color(0x662563EB)

@Composable
fun RentalConfirmationRoute(
    onNavigateBack: () -> Unit,
    onNavigateToActiveRental: (String) -> Unit,
    viewModel: RentalConfirmationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        viewModel.onIntent(RentalConfirmationIntent.BackClicked)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RentalConfirmationEffect.NavigateBack -> onNavigateBack()
                is RentalConfirmationEffect.NavigateToActiveRental -> onNavigateToActiveRental(effect.rentalId)
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
    val isDark = isSystemInDarkTheme()
    val surfaceColor = if (isDark) Color(0xFF111827) else Color.White
    val backgroundColor = if (isDark) Color(0xFF0B0F14) else Color(0xFFF8FAFC)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Rezervasyon Onayı", 
                        fontWeight = FontWeight.Bold, 
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onIntent(RentalConfirmationIntent.BackClicked) },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Geri",
                            tint = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        bottomBar = {
            Surface(
                color = surfaceColor,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = { onIntent(RentalConfirmationIntent.CompleteReservationClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = RenCarBlueGlow),
                        shape = RoundedCornerShape(16.dp),
                        enabled = state.isTermsAccepted && !state.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RenCarBlue,
                            contentColor = Color.White,
                            disabledContainerColor = RenCarBlue.copy(alpha = 0.45f)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Rezervasyonu Tamamla", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.error, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onIntent(RentalConfirmationIntent.LoadVehicle) },
                        colors = ButtonDefaults.buttonColors(containerColor = RenCarBlue)
                    ) {
                        Text("Tekrar Dene")
                    }
                }
            }
        } else if (state.vehicle != null) {
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = RenCarBlue
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = state.vehicle.brand,
                                style = MaterialTheme.typography.labelLarge,
                                color = RenCarBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = state.vehicle.model,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "${state.vehicle.plate} • %${state.vehicle.fuelPercent} Yakıt",
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Kiralama planı", 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Plan Seçenekleri
                Row(modifier = Modifier.fillMaxWidth()) {
                    PlanItem(
                        title = "Dakikalık",
                        price = state.minutelyPriceLabel,
                        isSelected = state.selectedPlan == RentalPlan.PER_MINUTE,
                        onClick = { onIntent(RentalConfirmationIntent.PlanSelected(RentalPlan.PER_MINUTE)) },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    PlanItem(
                        title = "Saatlik",
                        price = state.hourlyPriceLabel,
                        isSelected = state.selectedPlan == RentalPlan.HOURLY,
                        onClick = { onIntent(RentalConfirmationIntent.PlanSelected(RentalPlan.HOURLY)) },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    PlanItem(
                        title = "Günlük",
                        price = state.dailyPriceLabel,
                        isSelected = state.selectedPlan == RentalPlan.DAILY,
                        onClick = { onIntent(RentalConfirmationIntent.PlanSelected(RentalPlan.DAILY)) },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }

                if (state.selectedPlan.requiresScheduledEndDate()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIntent(RentalConfirmationIntent.DailyEndDatePickerClicked) },
                        color = surfaceColor,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9)),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "Planlanan iade tarihi",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                )
                                Text(
                                    text = state.dailyEndDate?.format(
                                        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("tr-TR"))
                                    ) ?: "Tarih seçin",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                )
                            }
                            Text(
                                text = "Değiştir",
                                color = RenCarBlue,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Fiyat Özeti
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = surfaceColor,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        PriceDetailItem("Ücretsiz rezervasyon süresi", state.freeReservationTime, isDark = isDark)
                        PriceDetailItem("Açılış ücreti", state.basePriceLabel, isDark = isDark)
                        PriceDetailItem("Servis / Sigorta bedeli", state.serviceFeeLabel, isDark = isDark)
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9)
                        )
                        
                        PriceDetailItem(
                            label = "Tahmini Toplam",
                            subtitle = state.estimatedDuration,
                            value = state.estimatedPriceLabel,
                            isTotal = true,
                            isDark = isDark,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bilgilendirme Bannerı
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = RenCarBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Aracın yanına ulaştığınızda kilitleri uygulama üzerinden açarak kiralamayı başlatabilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFFBFDBFE) else Color(0xFF475569),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Onay Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIntent(RentalConfirmationIntent.TermsAcceptedChanged(!state.isTermsAccepted)) },
                    color = Color.Transparent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = state.isTermsAccepted,
                            onCheckedChange = { onIntent(RentalConfirmationIntent.TermsAcceptedChanged(it)) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = RenCarBlue,
                                uncheckedColor = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
                            )
                        )
                        Text(
                            text = "Kullanım ve kiralama sözleşmesini onaylıyorum.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    if (state.showDailyEndDatePicker) {
        val minDate = RentalConfirmationViewModel.defaultDailyEndDate()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dailyEndDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
                ?: minDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )

        DatePickerDialog(
            onDismissRequest = { onIntent(RentalConfirmationIntent.DailyEndDatePickerDismissed) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis ?: return@TextButton
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        if (!selectedDate.isBefore(minDate)) {
                            onIntent(RentalConfirmationIntent.DailyEndDateSelected(selectedDate))
                        }
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(RentalConfirmationIntent.DailyEndDatePickerDismissed) }) {
                    Text("İptal")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun PlanItem(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Surface(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp, 
            color = if (isSelected) RenCarBlue else if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9)
        ),
        color = if (isSelected) RenCarBlue.copy(alpha = 0.1f) else if (isDark) Color(0xFF111827) else Color.White,
        tonalElevation = if (isSelected) 0.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isSelected) RenCarBlue else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = price,
                fontSize = 13.sp,
                color = if (isDark) Color.White else Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PriceDetailItem(
    label: String,
    value: String,
    isTotal: Boolean = false,
    isDark: Boolean,
    subtitle: String? = null,
) {
    val labelColor = if (isTotal) {
        if (isDark) Color.White else Color(0xFF0F172A)
    } else {
        if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    }
    val valueColor = if (isTotal) {
        RenCarBlue
    } else {
        if (isDark) Color.White else Color(0xFF0F172A)
    }
    val valueStyle = if (isTotal) {
        MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    } else {
        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
    }

    if (isTotal) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
            ) {
                Text(
                    text = label,
                    color = labelColor,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            MoneyText(
                amount = value,
                style = valueStyle,
                color = valueColor,
            )
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = labelColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f).padding(end = 16.dp),
        )
        MoneyText(
            amount = value,
            style = valueStyle,
            color = valueColor,
        )
    }
}

@Composable
private fun MoneyText(
    amount: String,
    style: TextStyle,
    color: Color,
) {
    val symbol = if (amount.startsWith("₺")) "₺" else ""
    val numeric = if (symbol.isNotEmpty()) amount.removePrefix("₺") else amount
    val numericStyle = style.copy(
        fontFeatureSettings = "tnum",
        letterSpacing = 0.sp,
    )
    val symbolStyle = style.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        if (symbol.isNotEmpty()) {
            Text(
                text = symbol,
                style = symbolStyle,
                color = color,
            )
        }
        Text(
            text = numeric,
            style = numericStyle,
            color = color,
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
        )
    }
}
