package com.turkcell.rencarapp.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SplashEffect>(Channel.BUFFERED)
    val effect: Flow<SplashEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.StartClicked -> sendEffect(SplashEffect.NavigateToOnboarding)
            SplashIntent.LoginClicked -> sendEffect(SplashEffect.NavigateToLogin)
        }
    }

    private fun sendEffect(effect: SplashEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
