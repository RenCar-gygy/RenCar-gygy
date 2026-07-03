package com.turkcell.rencarapp.ui.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.auth.UserRole
import com.turkcell.rencarapp.data.license.LicenseRepository
import com.turkcell.rencarapp.data.license.LicenseStatus
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
class LicenseViewModel @Inject constructor(
    private val licenseRepository: LicenseRepository,
    private val sessionStore: SessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LicenseUiState())
    val uiState: StateFlow<LicenseUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LicenseEffect>(Channel.BUFFERED)
    val effect: Flow<LicenseEffect> = _effect.receiveAsFlow()

    init {
        loadStatus()
    }

    fun onIntent(intent: LicenseIntent) {
        when (intent) {
            LicenseIntent.BackClicked -> sendEffect(LicenseEffect.NavigateBack)
            LicenseIntent.UploadBackClicked -> uploadBack()
            LicenseIntent.ContinueClicked -> continueFlow()
        }
    }

    private fun loadStatus() {
        viewModelScope.launch {
            licenseRepository.getStatus()
                .onSuccess { info ->
                    _uiState.update {
                        when (info.status) {
                            LicenseStatus.APPROVED,
                            LicenseStatus.UNDER_REVIEW,
                            -> it.copy(
                                isBackUploaded = true,
                                isContinueEnabled = true,
                                rejectReason = null,
                            )
                            LicenseStatus.REJECTED -> it.copy(
                                rejectReason = info.rejectReason,
                            )
                            LicenseStatus.NOT_SUBMITTED -> it
                        }
                    }
                }
                .onFailure { error ->
                    sendEffect(LicenseEffect.ShowError(error.message ?: "Ehliyet durumu alınamadı."))
                }
        }
    }

    private fun uploadBack() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = licenseRepository.upload(
                frontImageBytes = STUB_IMAGE_BYTES,
                backImageBytes = STUB_IMAGE_BYTES,
            )
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isBackUploaded = true,
                            isContinueEnabled = true,
                            rejectReason = null,
                        )
                    }
                }
                .onFailure { error ->
                    sendEffect(LicenseEffect.ShowError(error.message ?: "Ehliyet yüklenemedi."))
                }
        }
    }

    private fun continueFlow() {
        if (!_uiState.value.isContinueEnabled || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val session = sessionStore.getSession()
            if (session == null) {
                _uiState.update { it.copy(isLoading = false) }
                sendEffect(LicenseEffect.ShowError("Oturum bulunamadı."))
                return@launch
            }

            sessionStore.saveSession(
                session.copy(user = session.user.copy(role = UserRole.CUSTOMER)),
            )
            _uiState.update { it.copy(isLoading = false) }
            sendEffect(LicenseEffect.NavigateToMain)
        }
    }

    private fun sendEffect(effect: LicenseEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private companion object {
        val STUB_IMAGE_BYTES = byteArrayOf(1)
    }
}
