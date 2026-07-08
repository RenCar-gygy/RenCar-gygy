package com.turkcell.rencarapp.ui.rental.delivery_photos

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
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
fun DeliveryPhotosRoute(
    onNavigateBack: () -> Unit,
    // GÜNCELLENDİ: NavHost ile eşleşmesi için Faturaya (Summary) yönlendiriyoruz
    onNavigateToSummary: (String) -> Unit,
    viewModel: DeliveryPhotosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DeliveryPhotosEffect.NavigateBack -> onNavigateBack()
                // ViewModel'i bozmamak için eski NavigateToActiveRental komutunu Summary'ye yönlendirdim
                is DeliveryPhotosEffect.NavigateToActiveRental -> onNavigateToSummary(effect.rentalId)
                is DeliveryPhotosEffect.ShowError -> {
                    Toast.makeText(context, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    DeliveryPhotosScreen(
        state = uiState,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryPhotosScreen(
    state: DeliveryPhotosUiState,
    onIntent: (DeliveryPhotosIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { onIntent(DeliveryPhotosIntent.BackClicked) },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFBC02D),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hasarları net çek — teslim sonrası anlaşmazlığı önler.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = { onIntent(DeliveryPhotosIntent.StartRentalClicked) }, // Adı aynı kaldı ki kodların çökmesin
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = state.isComplete && !state.isStartingRental,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E63D8), // Daha estetik mavi tonu
                        disabledContainerColor = Color(0xFFE0E0E0)
                    )
                ) {
                    if (state.isStartingRental) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        // GÜNCELLENDİ: Metinler "Kiralamayı başlat" yerine fotoğrafların bitişine uyarlandı
                        val buttonText = if (state.isComplete) "Fotoğrafları Onayla ve Bitir" else "Eksik Fotoğrafları Tamamla"
                        Text(buttonText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Araç teslimi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Bitirmeden önce 4 yönü çek",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.vehicleName} - ${state.plate}",
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Text(
                    text = "${state.capturedCount} / 4 çekildi",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E63D8)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Grid
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    PhotoBox(
                        direction = PhotoDirection.FRONT,
                        uri = state.photos[PhotoDirection.FRONT],
                        onClick = {
                            onIntent(DeliveryPhotosIntent.PhotoCaptured(PhotoDirection.FRONT, Uri.parse("mock_uri")))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    PhotoBox(
                        direction = PhotoDirection.BACK,
                        uri = state.photos[PhotoDirection.BACK],
                        onClick = {
                            onIntent(DeliveryPhotosIntent.PhotoCaptured(PhotoDirection.BACK, Uri.parse("mock_uri")))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    PhotoBox(
                        direction = PhotoDirection.LEFT,
                        uri = state.photos[PhotoDirection.LEFT],
                        onClick = {
                            onIntent(DeliveryPhotosIntent.PhotoCaptured(PhotoDirection.LEFT, Uri.parse("mock_uri")))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    PhotoBox(
                        direction = PhotoDirection.RIGHT,
                        uri = state.photos[PhotoDirection.RIGHT],
                        onClick = {
                            onIntent(DeliveryPhotosIntent.PhotoCaptured(PhotoDirection.RIGHT, Uri.parse("mock_uri")))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoBox(
    direction: PhotoDirection,
    uri: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCaptured = uri != null

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCaptured) Color(0xFFE0F2F1) else Color.White)
            .then(
                if (!isCaptured) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isCaptured) Icons.Default.DirectionsCar else Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = if (isCaptured) Color(0xFF80CBC4) else Color(0xFF1E63D8),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fotoğraf çek",
                fontSize = 12.sp,
                color = if (isCaptured) Color.Transparent else Color.Gray
            )
        }

        // Direction Tag
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            color = if (isCaptured) Color(0xFF263238) else Color(0xFFF5F5F5),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = direction.label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCaptured) Color.White else Color.Gray
            )
        }

        // Checkmark
        if (isCaptured) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
            )
        }
    }
}