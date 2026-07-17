package com.turkcell.rencarapp.ui.rental.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material.icons.filled.Close
import androidx.hilt.navigation.compose.hiltViewModel
import com.turkcell.rencarapp.data.rental.PaymentMethod
import com.turkcell.rencarapp.ui.common.IyzicoCheckoutSupport
import com.turkcell.rencarapp.ui.common.applyPaymentPageSettings
import com.turkcell.rencarapp.ui.common.ensureMobileViewport

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

    if (uiState.showAddCardSheet) {
        AddPaymentCardBottomSheet(
            isSubmitting = uiState.isAddingCard,
            onIntent = viewModel::onIntent,
        )
    }

    if (uiState.showIyzicoCheckout &&
        (
            !uiState.iyzicoPaymentPageUrl.isNullOrBlank() ||
                !uiState.iyzicoCheckoutFormContent.isNullOrBlank()
            )
    ) {
        IyzicoCheckoutDialog(
            paymentPageUrl = uiState.iyzicoPaymentPageUrl,
            checkoutFormContent = uiState.iyzicoCheckoutFormContent,
            expiresAtEpochMs = uiState.iyzicoCheckoutExpiresAtEpochMs,
            onCheckoutCompleted = { viewModel.onIntent(RentalSummaryIntent.IyzicoCheckoutCompleted) },
            onSessionExpired = { viewModel.onIntent(RentalSummaryIntent.IyzicoCheckoutSessionExpired) },
            onDismiss = { viewModel.onIntent(RentalSummaryIntent.DismissIyzicoCheckout) },
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
                                    text = "Ödemeyi Yap",
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

            // --- FATURA DETAYI KARTI (kalan alanda kaydırılabilir) ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Fatura Detayı",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Default,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                        if (!uiState.isPaid) {
                            Text(
                                text = "Ödeme Yöntemi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            PaymentMethodSelector(
                                uiState = uiState,
                                onIntent = onIntent,
                            )
                            if (uiState.selectedMethod == PaymentMethod.IYZICO) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = "Ödeme, İyzico'nun güvenli ödeme sayfasında yapılır. Kart bilgin uygulamada tutulmaz.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        text = "İndirim kodu yalnızca cüzdan veya kayıtlı kart ile kullanılabilir; İyzico ödemesinde geçerli değildir.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        FeeRow("Kiralama Süresi", uiState.durationText)
                        FeeRow("Katedilen Mesafe", uiState.distanceText)

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                        FeeRow("Kullanım Ücreti", uiState.rentalFee)
                        if (uiState.startFee.isNotEmpty()) {
                            FeeRow("Açılış Ücreti", uiState.startFee)
                        }
                        FeeRow("Hizmet Bedeli", uiState.serviceFee)

                        if (!uiState.isPaid && uiState.selectedMethod != PaymentMethod.IYZICO) {
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Toplam Ödenecek",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Default,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = uiState.totalFee,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Default,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PaymentMethodSelector(
    uiState: RentalSummaryUiState,
    onIntent: (RentalSummaryIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PaymentMethodTile(
            label = "Cüzdan",
            icon = Icons.Default.AccountBalanceWallet,
            selected = uiState.selectedMethod == PaymentMethod.WALLET,
            onClick = { onIntent(RentalSummaryIntent.SelectWalletPayment) },
            modifier = Modifier.weight(1f),
        )
        PaymentMethodTile(
            label = "Kart",
            icon = Icons.Default.CreditCard,
            selected = uiState.selectedMethod == PaymentMethod.CARD,
            onClick = { onIntent(RentalSummaryIntent.ChangePaymentMethodClicked) },
            modifier = Modifier.weight(1f),
        )
        PaymentMethodTile(
            label = "İyzico",
            icon = Icons.Outlined.Payment,
            selected = uiState.selectedMethod == PaymentMethod.IYZICO,
            onClick = { onIntent(RentalSummaryIntent.SelectIyzicoPayment) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PaymentMethodTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

private const val IYZICO_CALLBACK_PATH = "/iyzico/checkout-form/callback"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IyzicoCheckoutDialog(
    paymentPageUrl: String?,
    checkoutFormContent: String?,
    expiresAtEpochMs: Long?,
    onCheckoutCompleted: () -> Unit,
    onSessionExpired: () -> Unit,
    onDismiss: () -> Unit,
) {
    val finished = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(expiresAtEpochMs) {
        val expiresAt = expiresAtEpochMs ?: return@LaunchedEffect
        val remainingMs = expiresAt - System.currentTimeMillis()
        if (remainingMs <= 0L) {
            if (!finished.value) {
                finished.value = true
                onSessionExpired()
            }
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(remainingMs)
        if (!finished.value) {
            finished.value = true
            onSessionExpired()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            text = "İyzico ile Öde",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { context ->
                        WebView(context).apply {
                            applyPaymentPageSettings()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                ): Boolean {
                                    if (IyzicoCheckoutSupport.isSessionExpired(expiresAtEpochMs)) {
                                        if (!finished.value) {
                                            finished.value = true
                                            onSessionExpired()
                                        }
                                        return true
                                    }
                                    val url = request?.url?.toString().orEmpty()
                                    if (url.contains(IYZICO_CALLBACK_PATH)) {
                                        if (!finished.value) {
                                            finished.value = true
                                            onCheckoutCompleted()
                                        }
                                        return true
                                    }
                                    return false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    view?.ensureMobileViewport()
                                    if (IyzicoCheckoutSupport.isSessionExpired(expiresAtEpochMs)) {
                                        if (!finished.value) {
                                            finished.value = true
                                            onSessionExpired()
                                        }
                                        return
                                    }
                                    val loadedUrl = url.orEmpty()
                                    if (loadedUrl.contains(IYZICO_CALLBACK_PATH)) {
                                        if (!finished.value) {
                                            finished.value = true
                                            onCheckoutCompleted()
                                        }
                                    }
                                }
                            }
                            when {
                                !paymentPageUrl.isNullOrBlank() -> loadUrl(paymentPageUrl)
                                !checkoutFormContent.isNullOrBlank() -> loadDataWithBaseURL(
                                    IyzicoCheckoutSupport.wrapCheckoutFormHtml(checkoutFormContent),
                                    IyzicoCheckoutSupport.CHECKOUT_FORM_BASE_URL,
                                    "text/html",
                                    "UTF-8",
                                    null,
                                )
                            }
                        }
                    },
                )
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
            LazyColumn(
                modifier = Modifier.heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
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
                            },
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntent(RentalSummaryIntent.SelectIyzicoPayment) },
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.selectedMethod == PaymentMethod.IYZICO) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Payment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "İyzico",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = "İndirim kodu bu yöntemde kullanılamaz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (uiState.availableCards.isEmpty()) {
                Text(
                    text = "Henüz kayıtlı kartın yok. «Yeni Kart Ekle» ile devam edebilirsin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntent(RentalSummaryIntent.AddCardClicked) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Yeni Kart Ekle",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = "Kayıtlı kartını buradan ekleyebilirsin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPaymentCardBottomSheet(
    isSubmitting: Boolean,
    onIntent: (RentalSummaryIntent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var number by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { onIntent(RentalSummaryIntent.DismissAddCardSheet) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Yeni Kart Ekle",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = "Güvenlik için yalnızca kartın son 4 hanesi kaydedilir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = number,
                onValueChange = { if (it.length <= 16) number = it.filter { c -> c.isDigit() } },
                label = { Text("Kart Numarası") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = expiry,
                onValueChange = { if (it.length <= 5) expiry = it },
                label = { Text("Son Kullanma (AA/YY)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting,
            )
            Button(
                onClick = {
                    if (number.length >= 15 && expiry.length >= 4) {
                        onIntent(RentalSummaryIntent.SubmitNewCard(number, expiry))
                    }
                },
                enabled = !isSubmitting && number.length >= 15 && expiry.length >= 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Kartı Kaydet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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