package com.turkcell.rencarapp.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencarapp.ui.theme.RenCarAppTheme
import kotlinx.coroutines.flow.distinctUntilChanged

private val RenCarBlue = Color(0xFF2563EB)
private val RenCarBlueGlow = Color(0x662563EB)

private data class OnboardingColors(
    val background: Color,
    val title: Color,
    val subtitle: Color,
    val inactiveDot: Color,
    val stubBadge: Color,
    val stubBadgeText: Color,
    val buttonShadow: Color,
)

@Composable
private fun onboardingColors(darkTheme: Boolean): OnboardingColors =
    if (darkTheme) {
        OnboardingColors(
            background = Color(0xFF0B0F14),
            title = Color.White,
            subtitle = Color(0xFF94A3B8),
            inactiveDot = Color(0xFF334155),
            stubBadge = Color(0xFF1E293B),
            stubBadgeText = Color(0xFF64748B),
            buttonShadow = RenCarBlueGlow,
        )
    } else {
        OnboardingColors(
            background = Color.White,
            title = Color(0xFF0F172A),
            subtitle = Color(0xFF64748B),
            inactiveDot = Color(0xFFCBD5E1),
            stubBadge = Color(0xFFF1F5F9),
            stubBadgeText = Color(0xFF64748B),
            buttonShadow = Color(0x402563EB),
        )
    }

@Composable
fun OnboardingRoute(
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingEffect.NavigateToRegister -> onNavigateToRegister()
            }
        }
    }

    OnboardingScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = onboardingColors(isSystemInDarkTheme())
    val onboardingPageCount = state.pageCount - 1
    val pagerState = rememberPagerState(
        initialPage = state.currentPage - 1,
        pageCount = { onboardingPageCount },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                onIntent(OnboardingIntent.PageChanged(page + 1))
            }
    }

    LaunchedEffect(state.currentPage) {
        val targetPage = state.currentPage - 1
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    val isLastPage = state.currentPage == state.pageCount - 1

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

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                OnboardingStubPage(
                    pageNumber = page + 2,
                    colors = colors,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            OnboardingPageIndicator(
                pageCount = state.pageCount,
                currentPage = state.currentPage,
                colors = colors,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (isLastPage) {
                        onIntent(OnboardingIntent.FinishClicked)
                    } else {
                        onIntent(OnboardingIntent.ContinueClicked)
                    }
                },
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
                    text = if (isLastPage) "Hemen Başla" else "Devam Et",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnboardingStubPage(
    pageNumber: Int,
    colors: OnboardingColors,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = colors.buttonShadow)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.stubBadge),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = pageNumber.toString(),
                color = colors.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Onboarding",
            color = colors.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Sayfa $pageNumber — tasarım bekleniyor",
            color = colors.subtitle,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    colors: OnboardingColors,
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

@Preview(showBackground = true, name = "Onboarding Light")
@Composable
private fun OnboardingScreenLightPreview() {
    RenCarAppTheme(darkTheme = false, dynamicColor = false) {
        OnboardingScreen(
            state = OnboardingUiState(currentPage = 1),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, name = "Onboarding Dark")
@Composable
private fun OnboardingScreenDarkPreview() {
    RenCarAppTheme(darkTheme = true, dynamicColor = false) {
        OnboardingScreen(
            state = OnboardingUiState(currentPage = 2),
            onIntent = {},
        )
    }
}
