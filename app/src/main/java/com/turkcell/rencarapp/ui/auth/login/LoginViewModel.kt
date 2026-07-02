package com.turkcell.rencarapp.ui.auth.login

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
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect: Flow<LoginEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneChanged -> updatePhone(intent.value)
            LoginIntent.BackClicked -> sendEffect(LoginEffect.NavigateBack)
            LoginIntent.SendCodeClicked -> sendCode()
            LoginIntent.RegisterClicked -> sendEffect(LoginEffect.NavigateToRegister)
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
        sendEffect(LoginEffect.NavigateToOtp(phoneNumber = phone))
    }

    private fun sendEffect(effect: LoginEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
