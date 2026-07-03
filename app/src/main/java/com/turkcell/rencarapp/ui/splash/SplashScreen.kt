package com.turkcell.rencarapp.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencarapp.ui.theme.RenCarAppTheme

private val RenCarBlue = Color(0xFF2563EB)
private val RenCarBlueGlow = Color(0x662563EB)

private data class SplashColors(
    val background: Color,
    val title: Color,
    val subtitle: Color,
    val inactiveDot: Color,
    val footerText: Color,
    val logoContainer: Color,
    val logoGlow: Color,
    val buttonShadow: Color,
)

@Composable
private fun splashColors(darkTheme: Boolean): SplashColors =
    if (darkTheme) {
        SplashColors(
            background = Color(0xFF0B0F14),
            title = Color.White,
            subtitle = Color(0xFF94A3B8),
            inactiveDot = Color(0xFF334155),
            footerText = Color(0xFF64748B),
            logoContainer = RenCarBlue,
            logoGlow = RenCarBlueGlow,
            buttonShadow = RenCarBlueGlow,
        )
    } else {
        SplashColors(
            background = Color.White,
            title = Color(0xFF0F172A),
            subtitle = Color(0xFF64748B),
            inactiveDot = Color(0xFFCBD5E1),
            footerText = Color(0xFF64748B),
            logoContainer = RenCarBlue,
            logoGlow = Color(0x332563EB),
            buttonShadow = Color(0x402563EB),
        )
    }

@Composable
fun SplashRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToLicense: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SplashEffect.NavigateToOnboarding -> onNavigateToOnboarding()
                SplashEffect.NavigateToLogin -> onNavigateToLogin()
                SplashEffect.NavigateToLicense -> onNavigateToLicense()
                SplashEffect.NavigateToMain -> onNavigateToMain()
            }
        }
    }

    SplashScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun SplashScreen(
    state: SplashUiState,
    onIntent: (SplashIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = splashColors(isSystemInDarkTheme())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            SplashLogo(colors = colors)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Rencar",
                color = colors.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Yakındaki aracı bul, dakikalar\niçinde yola çık.",
                color = colors.subtitle,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            if (!state.isCheckingSession) {
                SplashPageIndicator(
                    pageCount = state.pageCount,
                    currentPage = state.currentPage,
                    colors = colors,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { onIntent(SplashIntent.StartClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = colors.buttonShadow),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RenCarBlue,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = "Hemen Başla",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LoginFooterText(
                    colors = colors,
                    onLoginClick = { onIntent(SplashIntent.LoginClicked) },
                )

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SplashLogo(colors: SplashColors) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.logoGlow, Color.Transparent),
                    ),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = colors.logoGlow)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.logoContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Composable
private fun SplashPageIndicator(
    pageCount: Int,
    currentPage: Int,
    colors: SplashColors,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (index == currentPage) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(RenCarBlue),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.inactiveDot),
                )
            }
        }
    }
}

@Composable
private fun LoginFooterText(
    colors: SplashColors,
    onLoginClick: () -> Unit,
) {
    val annotated = remember(colors) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.footerText)) {
                append("Zaten hesabım var · ")
            }
            withStyle(
                SpanStyle(
                    color = RenCarBlue,
                    fontWeight = FontWeight.SemiBold,
                ),
            ) {
                append("Giriş yap")
            }
        }
    }

    Text(
        text = annotated,
        fontSize = 14.sp,
        modifier = Modifier.clickable(onClick = onLoginClick),
    )
}

@Preview(showBackground = true, name = "Splash Light")
@Composable
private fun SplashScreenLightPreview() {
    RenCarAppTheme(darkTheme = false, dynamicColor = false) {
        SplashScreen(state = SplashUiState(), onIntent = {})
    }
}

@Preview(showBackground = true, name = "Splash Dark")
@Composable
private fun SplashScreenDarkPreview() {
    RenCarAppTheme(darkTheme = true, dynamicColor = false) {
        SplashScreen(state = SplashUiState(), onIntent = {})
    }
}
