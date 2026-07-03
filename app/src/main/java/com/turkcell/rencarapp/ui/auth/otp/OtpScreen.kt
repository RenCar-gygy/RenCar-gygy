package com.turkcell.rencarapp.ui.auth.otp

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
private val RenCarBlueLight = Color(0x332563EB)

private data class OtpColors(
    val background: Color,
    val title: Color,
    val subtitle: Color,
    val subtitleHighlight: Color,
    val footerText: Color,
    val timerText: Color,
    val timerHighlight: Color,
    val backButtonBackground: Color,
    val backButtonBorder: Color,
    val backButtonIcon: Color,
    val iconContainer: Color,
    val iconTint: Color,
    val digitBackground: Color,
    val digitBorder: Color,
    val digitFocusedBorder: Color,
    val digitText: Color,
    val buttonShadow: Color,
)

@Composable
private fun otpColors(darkTheme: Boolean): OtpColors =
    if (darkTheme) {
        OtpColors(
            background = Color(0xFF0B0F14),
            title = Color.White,
            subtitle = Color(0xFF94A3B8),
            subtitleHighlight = Color.White,
            footerText = Color(0xFF64748B),
            timerText = Color(0xFF64748B),
            timerHighlight = Color.White,
            backButtonBackground = Color(0xFF111827),
            backButtonBorder = Color(0xFF334155),
            backButtonIcon = Color.White,
            iconContainer = RenCarBlueLight,
            iconTint = RenCarBlue,
            digitBackground = Color(0xFF111827),
            digitBorder = Color(0xFF334155),
            digitFocusedBorder = RenCarBlue,
            digitText = Color.White,
            buttonShadow = RenCarBlueGlow,
        )
    } else {
        OtpColors(
            background = Color.White,
            title = Color(0xFF0F172A),
            subtitle = Color(0xFF64748B),
            subtitleHighlight = Color(0xFF0F172A),
            footerText = Color(0xFF64748B),
            timerText = Color(0xFF94A3B8),
            timerHighlight = Color(0xFF0F172A),
            backButtonBackground = Color(0xFFF8FAFC),
            backButtonBorder = Color(0xFFE2E8F0),
            backButtonIcon = Color(0xFF0F172A),
            iconContainer = Color(0xFFEFF6FF),
            iconTint = RenCarBlue,
            digitBackground = Color.White,
            digitBorder = Color(0xFFE2E8F0),
            digitFocusedBorder = RenCarBlue,
            digitText = Color(0xFF0F172A),
            buttonShadow = Color(0x402563EB),
        )
    }

@Composable
fun OtpRoute(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToLicense: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OtpEffect.NavigateBack -> onNavigateBack()
                OtpEffect.NavigateToLogin -> onNavigateToLogin()
                OtpEffect.NavigateToLicense -> onNavigateToLicense()
                OtpEffect.NavigateToMain -> onNavigateToMain()
                is OtpEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    OtpScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun OtpScreen(
    state: OtpUiState,
    onIntent: (OtpIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = otpColors(isSystemInDarkTheme())
    val focusRequester = remember { FocusRequester() }
    val activeIndex = state.code.length.coerceAtMost(OtpUiState.CODE_LENGTH - 1)
    val timerLabel = remember(state.remainingSeconds) {
        val minutes = state.remainingSeconds / 60
        val seconds = state.remainingSeconds % 60
        "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    LaunchedEffect(focusRequester) {
        withFrameNanos {}
        focusRequester.requestFocus()
    }

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
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OtpBackButton(
                colors = colors,
                onClick = { onIntent(OtpIntent.BackClicked) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            OtpFeatureIcon(colors = colors)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Telefonunu doğrula",
                color = colors.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OtpSubtitle(
                formattedPhone = state.formattedPhone,
                colors = colors,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { focusRequester.requestFocus() },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                ) {
                    repeat(OtpUiState.CODE_LENGTH) { index ->
                        OtpDigitBox(
                            digit = state.code.getOrNull(index)?.toString().orEmpty(),
                            isFocused = index == activeIndex && state.code.length < OtpUiState.CODE_LENGTH,
                            colors = colors,
                            onClick = { focusRequester.requestFocus() },
                        )
                    }
                }

                BasicTextField(
                    value = state.code,
                    onValueChange = { onIntent(OtpIntent.CodeChanged(it)) },
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0f)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
                    cursorBrush = SolidColor(Color.Transparent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OtpResendRow(
                colors = colors,
                timerLabel = timerLabel,
                isResendEnabled = state.isResendEnabled,
                onResendClick = { onIntent(OtpIntent.ResendClicked) },
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onIntent(OtpIntent.VerifyClicked) },
                enabled = state.isVerifyEnabled && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
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
                    text = "Doğrula ve Devam Et",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ChangePhoneFooter(
                colors = colors,
                onChangePhoneClick = { onIntent(OtpIntent.ChangePhoneClicked) },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OtpBackButton(
    colors: OtpColors,
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
private fun OtpFeatureIcon(colors: OtpColors) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.iconContainer),
        contentAlignment = Alignment.Center,
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Icon(
                imageVector = Icons.Outlined.Smartphone,
                contentDescription = null,
                tint = colors.iconTint,
                modifier = Modifier.size(28.dp),
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(RenCarBlue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp),
                )
            }
        }
    }
}

@Composable
private fun OtpSubtitle(
    formattedPhone: String,
    colors: OtpColors,
) {
    val annotated = remember(formattedPhone, colors) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.subtitleHighlight, fontWeight = FontWeight.SemiBold)) {
                append(formattedPhone)
            }
            withStyle(SpanStyle(color = colors.subtitle)) {
                append(" numarasına gönderdiğimiz 6 haneli kodu gir.")
            }
        }
    }

    Text(
        text = annotated,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    colors: OtpColors,
    onClick: () -> Unit,
) {
    val borderColor = if (isFocused) colors.digitFocusedBorder else colors.digitBorder
    val borderWidth = if (isFocused) 1.5.dp else 1.dp

    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.digitBackground)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (digit.isNotEmpty()) {
            Text(
                text = digit,
                color = colors.digitText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        } else if (isFocused) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .background(colors.digitFocusedBorder),
            )
        }
    }
}

@Composable
private fun OtpResendRow(
    colors: OtpColors,
    timerLabel: String,
    isResendEnabled: Boolean,
    onResendClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = if (isResendEnabled) Modifier.clickable(onClick = onResendClick) else Modifier,
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            tint = colors.timerText,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = if (isResendEnabled) RenCarBlue else colors.timerText)) {
                    append("Kodu tekrar gönder")
                }
                if (!isResendEnabled) {
                    withStyle(SpanStyle(color = colors.timerText)) {
                        append(" · ")
                    }
                    withStyle(SpanStyle(color = colors.timerHighlight, fontWeight = FontWeight.SemiBold)) {
                        append(timerLabel)
                    }
                }
            },
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun ChangePhoneFooter(
    colors: OtpColors,
    onChangePhoneClick: () -> Unit,
) {
    val annotated = remember(colors) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.footerText)) {
                append("Numara yanlış mı? ")
            }
            withStyle(SpanStyle(color = RenCarBlue, fontWeight = FontWeight.SemiBold)) {
                append("Değiştir")
            }
        }
    }

    Text(
        text = annotated,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onChangePhoneClick),
        textAlign = TextAlign.Center,
    )
}

@Preview(showBackground = true, name = "OTP Light")
@Composable
private fun OtpScreenLightPreview() {
    RenCarAppTheme(darkTheme = false, dynamicColor = false) {
        OtpScreen(
            state = OtpUiState(
                phoneNumber = "5320000000",
                formattedPhone = "+90 532 000 00 00",
                code = "482",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, name = "OTP Dark")
@Composable
private fun OtpScreenDarkPreview() {
    RenCarAppTheme(darkTheme = true, dynamicColor = false) {
        OtpScreen(
            state = OtpUiState(
                phoneNumber = "5320000000",
                formattedPhone = "+90 532 000 00 00",
                code = "482",
                isVerifyEnabled = false,
            ),
            onIntent = {},
        )
    }
}
