package com.turkcell.rencarapp.ui.license

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
class LicenseViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LicenseUiState())
    val uiState: StateFlow<LicenseUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LicenseEffect>(Channel.BUFFERED)
    val effect: Flow<LicenseEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LicenseIntent) {
        when (intent) {
            LicenseIntent.BackClicked -> sendEffect(LicenseEffect.NavigateBack)
            LicenseIntent.UploadBackClicked -> markBackUploaded()
            LicenseIntent.ContinueClicked -> continueFlow()
        }
    }

    private fun markBackUploaded() {
        _uiState.update {
            it.copy(
                isBackUploaded = true,
                isContinueEnabled = true,
            )
        }
    }

    private fun continueFlow() {
        if (!_uiState.value.isContinueEnabled) return
        sendEffect(LicenseEffect.NavigateToMain)
    }

    private fun sendEffect(effect: LicenseEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
