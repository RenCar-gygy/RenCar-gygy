package com.turkcell.rencarapp.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class RegisterViewModel @Inject constructor() : ViewModel() {

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
        val phone = _uiState.value.phoneNumber
        if (!_uiState.value.isSendCodeEnabled || _uiState.value.isLoading) return
        sendEffect(RegisterEffect.NavigateToOtp(phoneNumber = phone))
    }

    private fun sendEffect(effect: RegisterEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
