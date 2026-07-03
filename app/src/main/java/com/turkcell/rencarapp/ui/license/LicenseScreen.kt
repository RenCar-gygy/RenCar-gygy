package com.turkcell.rencarapp.ui.license

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencarapp.ui.theme.RenCarAppTheme

private val RenCarBlue = Color(0xFF2563EB)
private val RenCarBlueGlow = Color(0x662563EB)
private val SuccessGreen = Color(0xFF22C55E)

private data class LicenseStep(val label: String)

private val licenseSteps = listOf(
    LicenseStep("Ehliyet"),
    LicenseStep("Selfie"),
    LicenseStep("Onay"),
)

private data class LicenseColors(
    val background: Color,
    val title: Color,
    val subtitle: Color,
    val sectionTitle: Color,
    val backButtonBackground: Color,
    val backButtonBorder: Color,
    val backButtonIcon: Color,
    val stepActive: Color,
    val stepInactive: Color,
    val stepLabelActive: Color,
    val stepLabelInactive: Color,
    val stepLineActive: Color,
    val stepLineInactive: Color,
    val previewGradientStart: Color,
    val previewGradientEnd: Color,
    val uploadBackground: Color,
    val uploadBorder: Color,
    val uploadText: Color,
    val infoBackground: Color,
    val infoText: Color,
    val buttonShadow: Color,
)

@Composable
private fun licenseColors(darkTheme: Boolean): LicenseColors =
    if (darkTheme) {
        LicenseColors(
            background = Color(0xFF0B0F14),
            title = Color.White,
            subtitle = Color(0xFF94A3B8),
            sectionTitle = Color.White,
            backButtonBackground = Color(0xFF111827),
            backButtonBorder = Color(0xFF334155),
            backButtonIcon = Color.White,
            stepActive = RenCarBlue,
            stepInactive = Color(0xFF334155),
            stepLabelActive = Color.White,
            stepLabelInactive = Color(0xFF64748B),
            stepLineActive = RenCarBlue,
            stepLineInactive = Color(0xFF334155),
            previewGradientStart = Color(0xFF1E3A5F),
            previewGradientEnd = Color(0xFF0F172A),
            uploadBackground = Color(0xFF111827),
            uploadBorder = Color(0xFF475569),
            uploadText = Color(0xFF94A3B8),
            infoBackground = Color(0xFF172554),
            infoText = Color(0xFFBFDBFE),
            buttonShadow = RenCarBlueGlow,
        )
    } else {
        LicenseColors(
            background = Color.White,
            title = Color(0xFF0F172A),
            subtitle = Color(0xFF64748B),
            sectionTitle = Color(0xFF0F172A),
            backButtonBackground = Color(0xFFF8FAFC),
            backButtonBorder = Color(0xFFE2E8F0),
            backButtonIcon = Color(0xFF0F172A),
            stepActive = RenCarBlue,
            stepInactive = Color(0xFFE2E8F0),
            stepLabelActive = RenCarBlue,
            stepLabelInactive = Color(0xFF94A3B8),
            stepLineActive = RenCarBlue,
            stepLineInactive = Color(0xFFE2E8F0),
            previewGradientStart = Color(0xFFDBEAFE),
            previewGradientEnd = Color(0xFFEFF6FF),
            uploadBackground = Color(0xFFF8FAFC),
            uploadBorder = Color(0xFFCBD5E1),
            uploadText = Color(0xFF64748B),
            infoBackground = Color(0xFFEFF6FF),
            infoText = Color(0xFF475569),
            buttonShadow = Color(0x402563EB),
        )
    }

@Composable
fun LicenseRoute(
    onNavigateBack: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LicenseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LicenseEffect.NavigateBack -> onNavigateBack()
                LicenseEffect.NavigateToMain -> onNavigateToMain()
                is LicenseEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LicenseScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun LicenseScreen(
    state: LicenseUiState,
    onIntent: (LicenseIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = licenseColors(isSystemInDarkTheme())
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            LicenseBackButton(
                colors = colors,
                onClick = { onIntent(LicenseIntent.BackClicked) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ehliyet doğrulama",
                color = colors.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Kiralamadan önce tek seferlik",
                color = colors.subtitle,
                fontSize = 15.sp,
            )

            Spacer(modifier = Modifier.height(28.dp))

            LicenseStepper(
                activeStepIndex = state.activeStepIndex,
                colors = colors,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Ehliyet ön yüz",
                color = colors.sectionTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            LicenseFrontPreview(colors = colors)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ehliyet arka yüz",
                color = colors.sectionTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isBackUploaded) {
                LicenseBackPreview(colors = colors)
            } else {
                LicenseBackUploadPlaceholder(
                    colors = colors,
                    onClick = { onIntent(LicenseIntent.UploadBackClicked) },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LicenseInfoBanner(colors = colors)

            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = { onIntent(LicenseIntent.ContinueClicked) },
            enabled = state.isContinueEnabled && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = colors.buttonShadow),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RenCarBlue,
                contentColor = Color.White,
                disabledContainerColor = RenCarBlue.copy(alpha = 0.45f),
                disabledContentColor = Color.White.copy(alpha = 0.8f),
            ),
        ) {
            Text(
                text = "Devam Et",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LicenseBackButton(
    colors: LicenseColors,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.backButtonBackground)
            .border(1.dp, colors.backButtonBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Geri",
            tint = colors.backButtonIcon,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun LicenseStepper(
    activeStepIndex: Int,
    colors: LicenseColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        licenseSteps.forEachIndexed { index, step ->
            LicenseStepItem(
                stepNumber = index + 1,
                label = step.label,
                isActive = index == activeStepIndex,
                isCompleted = index < activeStepIndex,
                colors = colors,
            )
            if (index < licenseSteps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            if (index < activeStepIndex) colors.stepLineActive else colors.stepLineInactive,
                        ),
                )
            }
        }
    }
}

@Composable
private fun LicenseStepItem(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    colors: LicenseColors,
) {
    val circleColor = when {
        isActive || isCompleted -> colors.stepActive
        else -> colors.stepInactive
    }
    val labelColor = when {
        isActive -> colors.stepLabelActive
        else -> colors.stepLabelInactive
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stepNumber.toString(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun LicenseFrontPreview(colors: LicenseColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(colors.previewGradientStart, colors.previewGradientEnd),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(20.dp),
        ) {
            Text(
                text = "T.C.",
                color = colors.title.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SURUCU BELGESI",
                color = colors.title.copy(alpha = 0.5f),
                fontSize = 11.sp,
            )
        }

        UploadedBadge(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
        )
    }
}

@Composable
private fun LicenseBackPreview(colors: LicenseColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(colors.previewGradientEnd, colors.previewGradientStart),
                ),
            ),
    ) {
        UploadedBadge(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
        )
    }
}

@Composable
private fun UploadedBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SuccessGreen)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "Yüklendi",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LicenseBackUploadPlaceholder(
    colors: LicenseColors,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.uploadBackground)
            .drawBehind {
                drawRoundRect(
                    color = colors.uploadBorder,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f),
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RenCarBlue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Arka yüzü çek veya yükle",
                color = colors.uploadText,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun LicenseInfoBanner(colors: LicenseColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.infoBackground)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = RenCarBlue,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = remember(colors) {
                buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.infoText)) {
                        append("Bilgilerin güvenle saklanır. Doğrulama genelde ")
                    }
                    withStyle(SpanStyle(color = colors.infoText, fontWeight = FontWeight.Bold)) {
                        append("birkaç dakika")
                    }
                    withStyle(SpanStyle(color = colors.infoText)) {
                        append(" sürer.")
                    }
                }
            },
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
    }
}

@Preview(showBackground = true, name = "License Light")
@Composable
private fun LicenseScreenLightPreview() {
    RenCarAppTheme(darkTheme = false, dynamicColor = false) {
        LicenseScreen(state = LicenseUiState(), onIntent = {})
    }
}

@Preview(showBackground = true, name = "License Dark")
@Composable
private fun LicenseScreenDarkPreview() {
    RenCarAppTheme(darkTheme = true, dynamicColor = false) {
        LicenseScreen(
            state = LicenseUiState(isBackUploaded = true, isContinueEnabled = true),
            onIntent = {},
        )
    }
}
