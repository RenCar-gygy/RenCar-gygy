package com.turkcell.rencarapp.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.auth.AuthRepository
import com.turkcell.rencarapp.data.auth.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RegisterEffect>(Channel.BUFFERED)
    val effect: Flow<RegisterEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.FullNameChanged -> updateField { it.copy(fullName = intent.value.take(MAX_FULL_NAME_LENGTH)) }
            is RegisterIntent.EmailChanged -> updateField { it.copy(email = intent.value.trim().take(MAX_EMAIL_LENGTH)) }
            is RegisterIntent.PasswordChanged -> updateField {
                it.copy(password = intent.value.take(MAX_PASSWORD_LENGTH))
            }
            is RegisterIntent.PhoneChanged -> {
                val digits = intent.value.filter { it.isDigit() }.take(PHONE_DIGIT_LENGTH)
                updateField { it.copy(phoneNumber = digits) }
            }
            is RegisterIntent.ReferralCodeChanged -> updateField {
                it.copy(referralCode = intent.value.uppercase().take(MAX_REFERRAL_CODE_LENGTH))
            }
            RegisterIntent.BackClicked -> sendEffect(RegisterEffect.NavigateBack)
            RegisterIntent.RegisterClicked -> register()
            RegisterIntent.LoginClicked -> sendEffect(RegisterEffect.NavigateToLogin)
        }
    }

    private fun updateField(transform: (RegisterUiState) -> RegisterUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isRegisterEnabled = isFormValid(updated))
        }
    }

    private fun register() {
        val state = _uiState.value
        if (!state.isRegisterEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.register(
                email = state.email,
                password = state.password,
                fullName = state.fullName.trim(),
                phone = state.phoneNumber,
                referralCode = state.referralCode.trim().ifBlank { null },
            )
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { tokens ->
                    when (tokens.user.role) {
                        UserRole.PENDING -> sendEffect(RegisterEffect.NavigateToLicense)
                        UserRole.CUSTOMER -> sendEffect(RegisterEffect.NavigateToMain)
                        UserRole.ADMIN -> sendEffect(RegisterEffect.ShowError("Bu hesap türü desteklenmiyor."))
                    }
                }
                .onFailure { error ->
                    sendEffect(RegisterEffect.ShowError(error.message ?: "Kayıt tamamlanamadı."))
                }
        }
    }

    private fun isFormValid(state: RegisterUiState): Boolean =
        state.fullName.trim().length >= MIN_FULL_NAME_LENGTH &&
            isValidEmail(state.email) &&
            state.password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH &&
            isValidPhone(state.phoneNumber)

    private fun isValidEmail(email: String): Boolean {
        if (email.length < MIN_EMAIL_LENGTH) return false
        val atIndex = email.indexOf('@')
        return atIndex > 0 && atIndex < email.lastIndex && email.contains('.')
    }

    private fun isValidPhone(phone: String): Boolean =
        phone.length == PHONE_DIGIT_LENGTH && phone.startsWith('5')

    private fun sendEffect(effect: RegisterEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private companion object {
        const val MIN_FULL_NAME_LENGTH = 2
        const val MAX_FULL_NAME_LENGTH = 80
        const val MIN_EMAIL_LENGTH = 5
        const val MAX_EMAIL_LENGTH = 120
        const val MIN_PASSWORD_LENGTH = 6
        const val MAX_PASSWORD_LENGTH = 72
        const val PHONE_DIGIT_LENGTH = 10
        const val MAX_REFERRAL_CODE_LENGTH = 16
    }
}
