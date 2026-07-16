package com.turkcell.rencarapp.ui.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToSplash: () -> Unit,
    onNavigateToWallet: () -> Unit, // YENİ EKLENDİ
    onShowSnackbar: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProfileEffect.NavigateToSplash -> onNavigateToSplash()
                is ProfileEffect.NavigateToWallet -> onNavigateToWallet() // Cüzdana geçiş
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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.fullName.ifBlank { "Kullanıcı" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.phone.ifBlank { "Telefon bilgisi yok" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { onIntent(ProfileIntent.EditProfileClicked) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Düzenle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            val isVerified = state.isLicenseVerified
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVerified) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint = if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isVerified) "Ehliyet doğrulandı" else "Ehliyet onayı bekleniyor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isVerified) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onErrorContainer
                        )
                        if (isVerified) {
                            Text(
                                text = "B Sınıfı - geçerli",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                    if (isVerified) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFC8E6C9)
                        ) {
                            Text(
                                text = "Onaylı",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ProfileMenuItem(
                title = "Ödeme yöntemleri",
                icon = Icons.Rounded.Payment,
                onClick = { onIntent(ProfileIntent.MenuItemClicked("Ödeme yöntemleri")) }
            )
            ProfileMenuItem(
                title = "Ayarlar",
                icon = Icons.Rounded.Settings,
                onClick = { onIntent(ProfileIntent.MenuItemClicked("Ayarlar")) }
            )
            ProfileMenuItem(
                title = "Yardım & destek",
                icon = Icons.Rounded.HelpOutline,
                onClick = { onIntent(ProfileIntent.MenuItemClicked("Yardım & destek")) }
            )
            ProfileMenuItem(
                title = "Davet et - 50₺ kazan",
                icon = Icons.Rounded.Share,
                onClick = { onIntent(ProfileIntent.MenuItemClicked("Davet et")) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Tema Butonu En Altta, Çıkış Yap'ın Üstünde
            CustomThemeSwitch(
                isDark = state.isDarkMode,
                onToggle = { onIntent(ProfileIntent.ThemeToggleClicked) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onIntent(ProfileIntent.LogoutClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Çıkış yap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomThemeSwitch(
    isDark: Boolean,
    onToggle: () -> Unit
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (isDark) 40.dp else 4.dp,
        animationSpec = tween(durationMillis = 300),
        label = "thumbOffset"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF37474F) else Color(0xFF64B5F6),
        animationSpec = tween(durationMillis = 300),
        label = "bgColor"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFCFD8DC) else Color(0xFFFFD54F),
        animationSpec = tween(durationMillis = 300),
        label = "thumbColor"
    )

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onToggle() },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(32.dp)
                .clip(CircleShape)
                .background(thumbColor)
        ) {
            if (isDark) {
                Box(modifier = Modifier.size(6.dp).offset(x = 6.dp, y = 8.dp).clip(CircleShape).background(Color(0xFF90A4AE)))
                Box(modifier = Modifier.size(8.dp).offset(x = 18.dp, y = 14.dp).clip(CircleShape).background(Color(0xFF90A4AE)))
                Box(modifier = Modifier.size(5.dp).offset(x = 10.dp, y = 20.dp).clip(CircleShape).background(Color(0xFF90A4AE)))
            }
        }
    }
}