package com.turkcell.rencarapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToSplash: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProfileEffect.NavigateToSplash -> onNavigateToSplash()
                is ProfileEffect.ShowError -> onShowSnackbar(effect.message)
            }
        }
    }

    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            // Kullanıcı Bilgileri Başlığı
            Text(text = state.fullName, style = MaterialTheme.typography.titleLarge)
            Text(text = state.phone, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // Ehliyet Durumu Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isLicenseVerified) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                PaddingValues(16.dp)
                Text(
                    text = if (state.isLicenseVerified) "Ehliyet doğrulandı" else "Ehliyet onayı bekleniyor",
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menü Öğeleri
            ProfileMenuItem(title = "Ödeme yöntemleri") { onIntent(ProfileIntent.MenuItemClicked("Ödeme")) }
            ProfileMenuItem(title = "Ayarlar") { onIntent(ProfileIntent.MenuItemClicked("Ayarlar")) }
            ProfileMenuItem(title = "Yardım & destek") { onIntent(ProfileIntent.MenuItemClicked("Yardım")) }
            ProfileMenuItem(title = "Davet et - 50₺ kazan") { onIntent(ProfileIntent.MenuItemClicked("Davet")) }

            Spacer(modifier = Modifier.height(32.dp))

            // Çıkış Yap Butonu
            OutlinedButton(
                onClick = { onIntent(ProfileIntent.LogoutClicked) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Çıkış yap")
            }
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}