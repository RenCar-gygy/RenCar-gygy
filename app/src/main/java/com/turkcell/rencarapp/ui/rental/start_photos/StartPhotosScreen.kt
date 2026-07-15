package com.turkcell.rencarapp.ui.rental.start_photos

import android.Manifest
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencarapp.ui.rental.delivery_photos.PhotoBox
import com.turkcell.rencarapp.ui.rental.delivery_photos.PhotoDirection
import java.io.ByteArrayOutputStream

private val RenCarBlue = Color(0xFF2563EB)
private val RenCarBlueGlow = Color(0x662563EB)

@Composable
fun StartPhotosRoute(
    onNavigateBack: () -> Unit,
    onRideStarted: () -> Unit,
    viewModel: StartPhotosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingDirection by remember { mutableStateOf<PhotoDirection?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        val direction = pendingDirection
        pendingDirection = null
        if (bitmap != null && direction != null) {
            viewModel.onIntent(
                StartPhotosIntent.PhotoCaptured(
                    direction = direction,
                    bytes = bitmap.toPngByteArray(),
                ),
            )
        } else if (direction != null) {
            Toast.makeText(context, "Fotoğraf çekilmedi.", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            cameraLauncher.launch()
        } else {
            pendingDirection = null
            Toast.makeText(context, "Kamera izni gerekli.", Toast.LENGTH_LONG).show()
        }
    }

    fun launchCamera(direction: PhotoDirection) {
        pendingDirection = direction
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                cameraLauncher.launch()
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    BackHandler {
        viewModel.onIntent(StartPhotosIntent.BackClicked)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StartPhotosEffect.NavigateBack -> onNavigateBack()
                is StartPhotosEffect.RideStarted -> onRideStarted()
                is StartPhotosEffect.LaunchCamera -> launchCamera(effect.direction)
                is StartPhotosEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    StartPhotosScreen(
        state = uiState,
        onIntent = viewModel::onIntent
    )
}

private fun Bitmap.toPngByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartPhotosScreen(
    state: StartPhotosUiState,
    onIntent: (StartPhotosIntent) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surfaceColor = if (isDark) Color(0xFF111827) else Color.White
    val backgroundColor = if (isDark) Color(0xFF0B0F14) else Color(0xFFF8FAFC)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Araç Fotoğrafları", 
                        fontWeight = FontWeight.Bold, 
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onIntent(StartPhotosIntent.BackClicked) },
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
                        onClick = { onIntent(StartPhotosIntent.CompletePhotosClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = RenCarBlueGlow),
                        shape = RoundedCornerShape(16.dp),
                        enabled = state.isComplete && !state.isSubmittingPhotos,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RenCarBlue,
                            contentColor = Color.White,
                            disabledContainerColor = RenCarBlue.copy(alpha = 0.45f)
                        )
                    ) {
                        if (state.isSubmittingPhotos) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (state.isComplete) "Kilidi Aç ve Yolculuğu Başlat" else "Eksik Fotoğrafları Tamamla", 
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Yolculuk öncesi kontrol",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Text(
                text = "Aracın dört bir yanını çekerek güvenli sürüşe başlayın.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Araç Bilgi Barı
            Surface(
                color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.vehicleDisplayLabel,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    
                    Surface(
                        color = if (state.isComplete) Color(0xFF22C55E).copy(alpha = 0.1f) else RenCarBlue.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${state.capturedCount} / 4 Tamam",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = if (state.isComplete) Color(0xFF22C55E) else RenCarBlue,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fotoğraf Grid
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PhotoBox(
                        direction = PhotoDirection.FRONT,
                        uri = state.photos[PhotoDirection.FRONT],
                        onClick = { onIntent(StartPhotosIntent.PhotoCaptureRequested(PhotoDirection.FRONT)) },
                        modifier = Modifier.weight(1f)
                    )
                    PhotoBox(
                        direction = PhotoDirection.BACK,
                        uri = state.photos[PhotoDirection.BACK],
                        onClick = { onIntent(StartPhotosIntent.PhotoCaptureRequested(PhotoDirection.BACK)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PhotoBox(
                        direction = PhotoDirection.LEFT,
                        uri = state.photos[PhotoDirection.LEFT],
                        onClick = { onIntent(StartPhotosIntent.PhotoCaptureRequested(PhotoDirection.LEFT)) },
                        modifier = Modifier.weight(1f)
                    )
                    PhotoBox(
                        direction = PhotoDirection.RIGHT,
                        uri = state.photos[PhotoDirection.RIGHT],
                        onClick = { onIntent(StartPhotosIntent.PhotoCaptureRequested(PhotoDirection.RIGHT)) },
                        modifier = Modifier.weight(1f)
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
                        text = "Fotoğraflar kiralama öncesi aracın durumunu kaydetmek içindir. Hasarlı bölgeleri yakından çekebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFFBFDBFE) else Color(0xFF475569),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
