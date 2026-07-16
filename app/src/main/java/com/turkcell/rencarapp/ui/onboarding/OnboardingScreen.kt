package com.turkcell.rencarapp.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
private val EconomicOrange = Color(0xFFF97316)
private val ComfortPurple = Color(0xFFA855F7)

private data class OnboardingPageData(
    val title: String,
    val subtitle: String,
)

private val onboardingPages = listOf(
    OnboardingPageData(
        title = "Haritada keşfet",
        subtitle = "Yakınındaki müsait araçları gör,\nanında fiyat karşılaştır.",
    ),
    OnboardingPageData(
        title = "Hemen kirala",
        subtitle = "Kayıt ol, doğrulamayı tamamla\nve dakikalar içinde yola çık.",
    ),
)

private data class OnboardingColors(
    val background: Color,
    val title: Color,
    val subtitle: Color,
    val inactiveDot: Color,
    val illustrationBackground: Color,
    val illustrationSurface: Color,
    val illustrationRoad: Color,
    val illustrationAccent: Color,
    val stepCardBackground: Color,
    val stepCardBorder: Color,
    val stepLabel: Color,
    val searchBackground: Color,
    val searchBorder: Color,
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
            illustrationBackground = Color(0xFF1A1F2E),
            illustrationSurface = Color(0xFF252B3B),
            illustrationRoad = Color(0xFF2F3648),
            illustrationAccent = Color(0xFF1E3A2F),
            stepCardBackground = Color(0xFF111827),
            stepCardBorder = Color(0xFF334155),
            stepLabel = Color(0xFF94A3B8),
            searchBackground = Color(0xFF111827),
            searchBorder = Color(0xFF334155),
            buttonShadow = RenCarBlueGlow,
        )
    } else {
        OnboardingColors(
            background = Color.White,
            title = Color(0xFF0F172A),
            subtitle = Color(0xFF64748B),
            inactiveDot = Color(0xFFCBD5E1),
            illustrationBackground = Color(0xFFE8EDF3),
            illustrationSurface = Color(0xFFF5F7FA),
            illustrationRoad = Color.White,
            illustrationAccent = Color(0xFFD1FAE5),
            stepCardBackground = Color.White,
            stepCardBorder = Color(0xFFE2E8F0),
            stepLabel = Color(0xFF64748B),
            searchBackground = Color.White,
            searchBorder = Color(0xFFE2E8F0),
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
                OnboardingPageContent(
                    pageData = onboardingPages[page],
                    pageIndex = page,
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
private fun OnboardingPageContent(
    pageData: OnboardingPageData,
    pageIndex: Int,
    colors: OnboardingColors,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (pageIndex) {
            0 -> OnboardingMapIllustration(colors = colors)
            1 -> OnboardingStepsIllustration(colors = colors)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = pageData.title,
            color = colors.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = pageData.subtitle,
            color = colors.subtitle,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OnboardingMapIllustration(colors: OnboardingColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.illustrationBackground),
    ) {
        Box(
            modifier = Modifier
                .size(width = 120.dp, height = 80.dp)
                .offset(x = 24.dp, y = 32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.illustrationSurface),
        )
        Box(
            modifier = Modifier
                .size(width = 90.dp, height = 70.dp)
                .offset(x = 180.dp, y = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.illustrationSurface),
        )
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 60.dp)
                .offset(x = 110.dp, y = 120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.illustrationAccent),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .offset(y = 100.dp)
                .background(colors.illustrationRoad),
        )
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 140.dp)
                .offset(x = 160.dp, y = 50.dp)
                .background(colors.illustrationRoad),
        )

        OnboardingPricePin(
            label = "₺28",
            pinColor = EconomicOrange,
            modifier = Modifier.offset(x = 52.dp, y = 72.dp),
        )
        OnboardingPricePin(
            label = "₺38",
            pinColor = ComfortPurple,
            modifier = Modifier.offset(x = 196.dp, y = 88.dp),
        )
        OnboardingPricePin(
            label = "₺32",
            pinColor = RenCarBlue,
            modifier = Modifier.offset(x = 128.dp, y = 148.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.searchBackground)
                .border(1.dp, colors.searchBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = RenCarBlue,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Yakınındaki araçları ara...",
                color = colors.subtitle,
                fontSize = 13.sp,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(44.dp)
                .shadow(8.dp, CircleShape, spotColor = colors.buttonShadow)
                .clip(CircleShape)
                .background(colors.searchBackground)
                .border(1.dp, colors.searchBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = null,
                tint = RenCarBlue,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun OnboardingPricePin(
    label: String,
    pinColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(10.dp), spotColor = pinColor.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(10.dp))
            .background(pinColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun OnboardingStepsIllustration(colors: OnboardingColors) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(RenCarBlueGlow, Color.Transparent),
                        ),
                        shape = CircleShape,
                    ),
            )
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = colors.buttonShadow)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RenCarBlue),
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OnboardingStepCard(
                icon = Icons.Outlined.Smartphone,
                label = "Kayıt ol",
                stepNumber = "1",
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            OnboardingStepCard(
                icon = Icons.Outlined.Badge,
                label = "Doğrula",
                stepNumber = "2",
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            OnboardingStepCard(
                icon = Icons.Default.DirectionsCar,
                label = "Yola çık",
                stepNumber = "3",
                colors = colors,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun OnboardingStepCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    stepNumber: String,
    colors: OnboardingColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.stepCardBackground)
            .border(1.dp, colors.stepCardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(RenCarBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stepNumber,
                color = RenCarBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RenCarBlue,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            color = colors.stepLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
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

@Preview(showBackground = true, name = "Onboarding Page 2 Light")
@Composable
private fun OnboardingPage2LightPreview() {
    RenCarAppTheme(darkTheme = false, dynamicColor = false) {
        OnboardingScreen(
            state = OnboardingUiState(currentPage = 1),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, name = "Onboarding Page 3 Light")
@Composable
private fun OnboardingPage3LightPreview() {
    RenCarAppTheme(darkTheme = false, dynamicColor = false) {
        OnboardingScreen(
            state = OnboardingUiState(currentPage = 2),
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
