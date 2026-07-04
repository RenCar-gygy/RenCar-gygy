package com.turkcell.rencarapp.ui.auth.register

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

private data class RegisterColors(
    val background: Color,
    val title: Color,
    val subtitle: Color,
    val label: Color,
    val fieldBackground: Color,
    val fieldBorder: Color,
    val fieldFocusedBorder: Color,
    val fieldText: Color,
    val fieldPlaceholder: Color,
    val infoText: Color,
    val footerText: Color,
    val backButtonBackground: Color,
    val backButtonBorder: Color,
    val backButtonIcon: Color,
    val buttonShadow: Color,
)

@Composable
private fun registerColors(darkTheme: Boolean): RegisterColors =
    if (darkTheme) {
        RegisterColors(
            background = Color(0xFF0B0F14),
            title = Color.White,
            subtitle = Color(0xFF94A3B8),
            label = Color(0xFF64748B),
            fieldBackground = Color(0xFF111827),
            fieldBorder = Color(0xFF334155),
            fieldFocusedBorder = RenCarBlue,
            fieldText = Color.White,
            fieldPlaceholder = Color(0xFF64748B),
            infoText = Color(0xFF64748B),
            footerText = Color(0xFF64748B),
            backButtonBackground = Color(0xFF111827),
            backButtonBorder = Color(0xFF334155),
            backButtonIcon = Color.White,
            buttonShadow = RenCarBlueGlow,
        )
    } else {
        RegisterColors(
            background = Color.White,
            title = Color(0xFF0F172A),
            subtitle = Color(0xFF64748B),
            label = Color(0xFF64748B),
            fieldBackground = Color(0xFFF8FAFC),
            fieldBorder = Color(0xFFE2E8F0),
            fieldFocusedBorder = RenCarBlue,
            fieldText = Color(0xFF0F172A),
            fieldPlaceholder = Color(0xFF94A3B8),
            infoText = Color(0xFF94A3B8),
            footerText = Color(0xFF64748B),
            backButtonBackground = Color(0xFFF8FAFC),
            backButtonBorder = Color(0xFFE2E8F0),
            backButtonIcon = Color(0xFF0F172A),
            buttonShadow = Color(0x402563EB),
        )
    }

@Composable
fun RegisterRoute(
    onNavigateBack: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegisterEffect.NavigateBack -> onNavigateBack()
                is RegisterEffect.NavigateToOtp -> onNavigateToOtp(effect.phoneNumber)
                RegisterEffect.NavigateToLogin -> onNavigateToLogin()
                is RegisterEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    RegisterScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onIntent: (RegisterIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = registerColors(isSystemInDarkTheme())
    val formattedPhone = remember(state.phoneNumber) { formatPhoneNumber(state.phoneNumber) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            RegisterBackButton(
                colors = colors,
                onClick = { onIntent(RegisterIntent.BackClicked) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hesabını oluştur",
                color = colors.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Bilgilerini gir; kayıt sonrası telefonuna doğrulama kodu gönderilecek.",
                color = colors.subtitle,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )

            Spacer(modifier = Modifier.height(28.dp))

            RegisterFieldLabel(text = "Ad Soyad", colors = colors)
            Spacer(modifier = Modifier.height(8.dp))
            RegisterTextField(
                value = state.fullName,
                onValueChange = { onIntent(RegisterIntent.FullNameChanged(it)) },
                placeholder = "Ahmet Yılmaz",
                leadingIcon = Icons.Outlined.Person,
                colors = colors,
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegisterFieldLabel(text = "E-posta", colors = colors)
            Spacer(modifier = Modifier.height(8.dp))
            RegisterTextField(
                value = state.email,
                onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
                placeholder = "ornek@email.com",
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                colors = colors,
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegisterFieldLabel(text = "Parola", colors = colors)
            Spacer(modifier = Modifier.height(8.dp))
            RegisterTextField(
                value = state.password,
                onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
                placeholder = "En az 6 karakter",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                colors = colors,
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegisterFieldLabel(text = "Telefon numarası", colors = colors)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CountryCodeField(colors = colors)
                PhoneNumberField(
                    value = formattedPhone,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    onValueChange = { onIntent(RegisterIntent.PhoneChanged(it)) },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            RegisterInfoBanner(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { onIntent(RegisterIntent.RegisterClicked) },
                enabled = state.isRegisterEnabled && !state.isLoading,
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
                    text = if (state.isLoading) "Kaydediliyor..." else "Kayıt Ol",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LoginFooterText(
                colors = colors,
                onLoginClick = { onIntent(RegisterIntent.LoginClicked) },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RegisterFieldLabel(
    text: String,
    colors: RegisterColors,
) {
    Text(
        text = text,
        color = colors.label,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    colors: RegisterColors,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.fieldBackground)
            .border(1.dp, colors.fieldBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        textStyle = TextStyle(
            color = colors.fieldText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(colors.fieldFocusedBorder),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = colors.fieldPlaceholder,
                    modifier = Modifier.size(20.dp),
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = colors.fieldPlaceholder,
                            fontSize = 15.sp,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun RegisterInfoBanner(colors: RegisterColors) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = colors.infoText,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Kayıt sonrası telefonuna 6 haneli doğrulama kodu gönderilir. Test ortamında kod: 123456.",
            color = colors.infoText,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun RegisterBackButton(
    colors: RegisterColors,
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
private fun CountryCodeField(colors: RegisterColors) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.fieldBackground)
            .border(1.dp, colors.fieldBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "TR +90",
            color = colors.fieldText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PhoneNumberField(
    value: String,
    colors: RegisterColors,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.fieldBackground)
            .border(1.dp, colors.fieldBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        textStyle = TextStyle(
            color = colors.fieldText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        cursorBrush = SolidColor(colors.fieldFocusedBorder),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "532 000 00 00",
                        color = colors.fieldPlaceholder,
                        fontSize = 15.sp,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun LoginFooterText(
    colors: RegisterColors,
    onLoginClick: () -> Unit,
) {
    val annotated = remember(colors) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.footerText)) {
                append("Zaten hesabın var? ")
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLoginClick),
        textAlign = TextAlign.Center,
    )
}

private fun formatPhoneNumber(digits: String): String =
    buildString {
        digits.forEachIndexed { index, char ->
            if (index == 3 || index == 6 || index == 8) append(' ')
            append(char)
        }
    }

@Preview(showBackground = true, name = "Register Light")
@Composable
private fun RegisterScreenLightPreview() {
    RenCarAppTheme(darkTheme = false, dynamicColor = false) {
        RegisterScreen(
            state = RegisterUiState(
                fullName = "Ahmet Yılmaz",
                email = "ahmet@example.com",
                password = "123456",
                phoneNumber = "5320000000",
                isRegisterEnabled = true,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, name = "Register Dark")
@Composable
private fun RegisterScreenDarkPreview() {
    RenCarAppTheme(darkTheme = true, dynamicColor = false) {
        RegisterScreen(state = RegisterUiState(), onIntent = {})
    }
}
