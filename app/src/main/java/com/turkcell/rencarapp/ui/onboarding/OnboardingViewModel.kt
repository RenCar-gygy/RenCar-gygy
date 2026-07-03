package com.turkcell.rencarapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class OnboardingViewModel @Inject constructor(
    private val sessionStore: SessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effect: Flow<OnboardingEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.PageChanged -> {
                _uiState.update { it.copy(currentPage = intent.page.coerceIn(1, it.pageCount - 1)) }
            }
            OnboardingIntent.ContinueClicked -> {
                val nextPage = _uiState.value.currentPage + 1
                if (nextPage < _uiState.value.pageCount) {
                    _uiState.update { it.copy(currentPage = nextPage) }
                }
            }
            OnboardingIntent.FinishClicked -> finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        viewModelScope.launch {
            sessionStore.setOnboardingCompleted(true)
            sendEffect(OnboardingEffect.NavigateToRegister)
        }
    }

    private fun sendEffect(effect: OnboardingEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
