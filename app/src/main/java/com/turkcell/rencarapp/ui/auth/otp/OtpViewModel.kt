package com.turkcell.rencarapp.ui.auth.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.auth.AuthRepository
import com.turkcell.rencarapp.data.auth.UserRole
import com.turkcell.rencarapp.ui.navigation.RenCarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val phoneNumber: String =
        savedStateHandle.get<String>(RenCarDestination.ARG_PHONE_NUMBER).orEmpty()

    private val _uiState = MutableStateFlow(
        OtpUiState(
            phoneNumber = phoneNumber,
            formattedPhone = formatDisplayPhone(phoneNumber),
        ),
    )
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<OtpEffect>(Channel.BUFFERED)
    val effect: Flow<OtpEffect> = _effect.receiveAsFlow()

    private var countdownJob: Job? = null

    init {
        startCountdown()
    }

    fun onIntent(intent: OtpIntent) {
        when (intent) {
            is OtpIntent.CodeChanged -> updateCode(intent.value)
            OtpIntent.BackClicked -> sendEffect(OtpEffect.NavigateBack)
            OtpIntent.ResendClicked -> resendCode()
            OtpIntent.VerifyClicked -> verifyCode()
            OtpIntent.ChangePhoneClicked -> sendEffect(OtpEffect.NavigateToLogin)
        }
    }

    private fun updateCode(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(OtpUiState.CODE_LENGTH)
        _uiState.update {
            it.copy(
                code = digits,
                isVerifyEnabled = digits.length == OtpUiState.CODE_LENGTH,
            )
        }
    }

    private fun resendCode() {
        if (!_uiState.value.isResendEnabled || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, code = "", isVerifyEnabled = false) }
            val result = authRepository.requestOtp(phoneNumber)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { startCountdown() }
                .onFailure { error ->
                    sendEffect(OtpEffect.ShowError(error.message ?: "Kod tekrar gönderilemedi."))
                }
        }
    }

    private fun verifyCode() {
        val state = _uiState.value
        if (!state.isVerifyEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.verifyOtp(
                phone = phoneNumber,
                code = state.code,
            )
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { tokens ->
                    when (tokens.user.role) {
                        UserRole.PENDING -> sendEffect(OtpEffect.NavigateToLicense)
                        UserRole.CUSTOMER -> sendEffect(OtpEffect.NavigateToMain)
                        UserRole.ADMIN -> sendEffect(OtpEffect.ShowError("Bu hesap türü desteklenmiyor."))
                    }
                }
                .onFailure { error ->
                    sendEffect(OtpEffect.ShowError(error.message ?: "Doğrulama başarısız."))
                }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                remainingSeconds = OtpUiState.RESEND_SECONDS,
                isResendEnabled = false,
            )
        }
        countdownJob = viewModelScope.launch {
            var remaining = OtpUiState.RESEND_SECONDS
            while (remaining > 0) {
                delay(1_000)
                remaining--
                _uiState.update { state -> state.copy(remainingSeconds = remaining) }
            }
            _uiState.update { it.copy(isResendEnabled = true) }
        }
    }

    private fun sendEffect(effect: OtpEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun formatDisplayPhone(digits: String): String {
            if (digits.length != 10) return "+90 $digits"
            return "+90 ${digits.substring(0, 3)} ${digits.substring(3, 6)} " +
                "${digits.substring(6, 8)} ${digits.substring(8, 10)}"
        }
    }
}
