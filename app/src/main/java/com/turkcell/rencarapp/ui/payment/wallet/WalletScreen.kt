package com.turkcell.rencarapp.ui.payment.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WalletRoute(
    viewModel: WalletViewModel = hiltViewModel(),
    onShowSnackbar: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onIntent(WalletIntent.FetchInitialData)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is WalletEffect.ShowToast -> onShowSnackbar(effect.message)
                is WalletEffect.ShowError -> onShowSnackbar(effect.message)
            }
        }
    }

    WalletScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    state: WalletUiState,
    onIntent: (WalletIntent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
        ) {
            // Başlık
            item {
                Text(
                    text = "Cüzdanım",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bakiye Kartı
            item {
                BalanceCard(balance = state.balance, onAddBalance = { onIntent(WalletIntent.AddBalanceClicked) })
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Kayıtlı Kartlar Başlığı
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Kayıtlı Kartlarım", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.clickable { onIntent(WalletIntent.AddCardClicked) }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = "Ekle", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Ekle", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Kart Listesi
            if (state.savedCards.isEmpty() && !state.isLoading) {
                item {
                    EmptyStateMessage(message = "Henüz kayıtlı bir kartınız bulunmuyor.")
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                items(state.savedCards) { card ->
                    SavedCardItem(
                        card = card,
                        onSetDefault = { onIntent(WalletIntent.SetDefaultCardClicked(card.id)) },
                        onDelete = { onIntent(WalletIntent.DeleteCardClicked(card.id)) },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Son İşlemler Başlığı
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Son İşlemler", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // İşlemler Listesi
            if (state.recentTransactions.isEmpty() && !state.isLoading) {
                item {
                    EmptyStateMessage(message = "Geçmiş işleminiz bulunmuyor.")
                }
            } else {
                items(state.recentTransactions) { transaction ->
                    TransactionItem(transaction = transaction)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Yüklenme Durumu (Modern Overlay)
        AnimatedVisibility(visible = state.isLoading || state.isActionLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        if (state.showDepositDialog) {
            DepositDialog(onIntent = onIntent)
        }

        if (state.showCardBottomSheet) {
            AddCardBottomSheet(onIntent = onIntent)
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardBottomSheet(onIntent: (WalletIntent) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var number by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { onIntent(WalletIntent.DismissCardBottomSheet) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Yeni Kart Ekle", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text(
                text = "Güvenlik için yalnızca kartın son 4 hanesi kaydedilir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = number, onValueChange = { if (it.length <= 16) number = it.filter { c -> c.isDigit() } },
                label = { Text("Kart Numarası") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = expiry, onValueChange = { if (it.length <= 5) expiry = it },
                label = { Text("Son Kullanma (AA/YY)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (number.length >= 15 && expiry.length >= 4) {
                        onIntent(WalletIntent.SubmitNewCard(number, expiry))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Kartı Kaydet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DepositDialog(onIntent: (WalletIntent) -> Unit) {
    var amountText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onIntent(WalletIntent.DismissDepositDialog) },
        shape = RoundedCornerShape(24.dp),
        title = { Text("Bakiye Yükle", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText, onValueChange = { amountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Yüklenecek Tutar (TL)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Minimum 10 TL, maksimum 5000 TL",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) onIntent(WalletIntent.Deposit(amount))
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Yükle", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(WalletIntent.DismissDepositDialog) }) { Text("İptal") }
        }
    )
}

@Composable
fun BalanceCard(balance: String, onAddBalance: () -> Unit) {
    val gradientColors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298)) // Premium Koyu Mavi Degrade

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(colors = gradientColors))
                .padding(28.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Kullanılabilir Bakiye", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = balance,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onAddBalance,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1E3C72)),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(text = "+ Bakiye Yükle", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun SavedCardItem(
    card: CardUiModel,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
) {
    val isVisa = card.brand.uppercase().contains("VISA")
    val brandColor = if (isVisa) Color(0xFF1A1F71) else Color(0xFFFF5F00)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(brandColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = card.brand, color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "•••• •••• •••• ${card.last4}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Son kullanma: ${card.expiry}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (card.isDefault) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = "Varsayılan",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            if (!card.isDefault) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onSetDefault) {
                        Text("Varsayılan yap")
                    }
                    TextButton(onClick = onDelete) {
                        Text("Sil", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDelete) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionUiModel) {
    val iconBgColor = if (transaction.isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val iconTextColor = if (transaction.isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (transaction.isIncome) "+" else "-", color = iconTextColor, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaction.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = transaction.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = transaction.amount, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = if (transaction.isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface)
    }
}