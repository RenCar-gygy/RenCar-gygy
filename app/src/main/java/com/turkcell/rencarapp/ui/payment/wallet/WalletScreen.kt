package com.turkcell.rencarapp.ui.payment.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WalletRoute(
    viewModel: WalletViewModel = hiltViewModel(),
    onShowSnackbar: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is WalletEffect.ShowToast -> onShowSnackbar(effect.message)
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
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 80.dp)
        ) {
            item {
                Text(text = "Cüzdan (v2)", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp))
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                BalanceCard(balance = state.balance, onAddBalance = { onIntent(WalletIntent.AddBalanceClicked) })
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Kayıtlı kartlar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = "+ Ekle",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E68D7),
                        modifier = Modifier.clickable { onIntent(WalletIntent.AddCardClicked) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.savedCards.isEmpty() && !state.isLoading) {
                item {
                    Text(text = "Henüz kayıtlı bir v2 kartınız bulunmuyor.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                items(state.savedCards) { card ->
                    SavedCardItem(card = card)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Son işlemler", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(state.recentTransactions) { transaction ->
                TransactionItem(transaction = transaction)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (state.isLoading || state.isActionLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardBottomSheet(onIntent: (WalletIntent) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var holder by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { onIntent(WalletIntent.DismissCardBottomSheet) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "v2 Yeni Kart Ekle", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))

            OutlinedTextField(
                value = holder, onValueChange = { holder = it },
                label = { Text("Kart Üzerindeki İsim") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = number, onValueChange = { if (it.length <= 16) number = it },
                label = { Text("Kart Numarası") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = expiry, onValueChange = { if (it.length <= 5) expiry = it },
                label = { Text("Son Kullanma (AA/YY)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (holder.isNotBlank() && number.length >= 15 && expiry.length >= 4) {
                        onIntent(WalletIntent.SubmitNewCard(holder, number, expiry))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E68D7))
            ) {
                Text("Kartı Güvenle Kaydet (v2)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
        title = { Text("Bakiye Yükle (v2)", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = amountText, onValueChange = { amountText = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Tutar (TL)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount > 0) onIntent(WalletIntent.Deposit(amount))
            }) { Text("Yükle") }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(WalletIntent.DismissDepositDialog) }) { Text("İptal") }
        }
    )
}

@Composable
fun BalanceCard(balance: String, onAddBalance: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(brush = Brush.linearGradient(colors = listOf(Color(0xFF2A84FF), Color(0xFF1554C0))))
            .padding(24.dp)
    ) {
        Column {
            Text(text = "Rencar bakiyesi (v2)", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = balance, style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 36.sp), color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddBalance,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "+ Bakiye Yükle", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun SavedCardItem(card: CardUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(if (card.brand.uppercase().contains("VISA")) Color(0xFF1A1F71) else Color(0xFFEB001B)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = card.brand, color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "**** ${card.last4}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Son kullanma ${card.expiry}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(if (transaction.isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (transaction.isIncome) "+" else "-", color = if (transaction.isIncome) Color(0xFF2E7D32) else Color(0xFFC62828), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(text = transaction.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = transaction.amount, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold), color = if (transaction.isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface)
        }
    }
}