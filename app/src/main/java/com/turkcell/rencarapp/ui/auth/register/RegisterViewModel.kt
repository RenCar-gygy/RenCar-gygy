package com.turkcell.rencarapp.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.auth.AuthRepository
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
            is RegisterIntent.PhoneChanged -> updatePhone(intent.value)
            RegisterIntent.BackClicked -> sendEffect(RegisterEffect.NavigateBack)
            RegisterIntent.SendCodeClicked -> sendCode()
            RegisterIntent.LoginClicked -> sendEffect(RegisterEffect.NavigateToLogin)
        }
    }

    private fun updatePhone(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(10)
        _uiState.update {
            it.copy(
                phoneNumber = digits,
                isSendCodeEnabled = digits.length == 10 && digits.startsWith('5'),
            )
        }
    }

    private fun sendCode() {
        val state = _uiState.value
        if (!state.isSendCodeEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val phone = state.phoneNumber
            val result = authRepository.register(
                email = "$phone@rencar.local",
                password = REGISTER_STUB_PASSWORD,
                fullName = REGISTER_STUB_FULL_NAME,
                phone = phone,
            )
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { sendEffect(RegisterEffect.NavigateToOtp(phoneNumber = phone)) }
                .onFailure { error ->
                    sendEffect(RegisterEffect.ShowError(error.message ?: "Kayıt tamamlanamadı."))
                }
        }
    }

    private fun sendEffect(effect: RegisterEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private companion object {
        const val REGISTER_STUB_PASSWORD = "123456"
        const val REGISTER_STUB_FULL_NAME = "Kullanıcı"
    }
}
