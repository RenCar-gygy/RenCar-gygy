package com.turkcell.rencarapp.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.auth.AuthRepository
import com.turkcell.rencarapp.data.auth.UserRole
import com.turkcell.rencarapp.data.session.SessionStore
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
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SplashEffect>(Channel.BUFFERED)
    val effect: Flow<SplashEffect> = _effect.receiveAsFlow()

    init {
        resolveInitialDestination()
    }

    fun onIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.StartClicked -> sendEffect(SplashEffect.NavigateToOnboarding)
            SplashIntent.LoginClicked -> navigateToLogin(markOnboardingCompleted = true)
        }
    }

    private fun resolveInitialDestination() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingSession = true) }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    when (user.role) {
                        UserRole.CUSTOMER -> sendEffect(SplashEffect.NavigateToMain)
                        UserRole.PENDING -> sendEffect(SplashEffect.NavigateToLicense)
                        UserRole.ADMIN -> showSplashContent()
                    }
                }
                .onFailure {
                    if (sessionStore.isOnboardingCompleted()) {
                        sendEffect(SplashEffect.NavigateToLogin)
                    } else {
                        showSplashContent()
                    }
                }
        }
    }

    private fun navigateToLogin(markOnboardingCompleted: Boolean) {
        viewModelScope.launch {
            if (markOnboardingCompleted) {
                sessionStore.setOnboardingCompleted(true)
            }
            sendEffect(SplashEffect.NavigateToLogin)
        }
    }

    private fun showSplashContent() {
        _uiState.update { it.copy(isCheckingSession = false) }
    }

    private fun sendEffect(effect: SplashEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
