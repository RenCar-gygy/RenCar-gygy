package com.turkcell.rencarapp.ui.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.auth.AuthRepository
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
    private val authRepository: AuthRepository,
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
            LicenseIntent.UploadBackClicked -> markBackUploaded()
            LicenseIntent.UploadSelfieClicked -> uploadDocuments()
            LicenseIntent.ContinueClicked -> continueFlow()
        }
    }

    private fun loadStatus() {
        viewModelScope.launch {
            licenseRepository.getStatus()
                .onSuccess { info -> applyLicenseInfo(info.status, info.rejectReason) }
                .onFailure { error ->
                    sendEffect(LicenseEffect.ShowError(error.message ?: "Ehliyet durumu alınamadı."))
                }
        }
    }

    private fun applyLicenseInfo(status: LicenseStatus, rejectReason: String?) {
        _uiState.update {
            when (status) {
                LicenseStatus.APPROVED -> it.copy(
                    isBackUploaded = true,
                    isSelfieUploaded = true,
                    activeStepIndex = 2,
                    isContinueEnabled = true,
                    rejectReason = null,
                )
                LicenseStatus.UNDER_REVIEW -> it.copy(
                    isBackUploaded = true,
                    isSelfieUploaded = true,
                    activeStepIndex = 2,
                    isContinueEnabled = false,
                    rejectReason = null,
                )
                LicenseStatus.REJECTED -> it.copy(
                    isBackUploaded = false,
                    isSelfieUploaded = false,
                    activeStepIndex = 0,
                    isContinueEnabled = false,
                    rejectReason = rejectReason,
                )
                LicenseStatus.NOT_SUBMITTED -> it.copy(
                    isBackUploaded = false,
                    isSelfieUploaded = false,
                    activeStepIndex = 0,
                    isContinueEnabled = false,
                    rejectReason = null,
                )
            }
        }
    }

    private fun markBackUploaded() {
        if (_uiState.value.isLoading || _uiState.value.isBackUploaded) return
        _uiState.update {
            it.copy(
                isBackUploaded = true,
                activeStepIndex = 1,
            )
        }
    }

    private fun uploadDocuments() {
        val state = _uiState.value
        if (state.isLoading || !state.isBackUploaded || state.isSelfieUploaded) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = licenseRepository.upload(
                frontImageBytes = STUB_IMAGE_BYTES,
                backImageBytes = STUB_IMAGE_BYTES,
                selfieImageBytes = STUB_IMAGE_BYTES,
            )
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { info ->
                    applyLicenseInfo(info.status, info.rejectReason)
                    if (info.status == LicenseStatus.UNDER_REVIEW) {
                        sendEffect(
                            LicenseEffect.ShowError(
                                "Ehliyetiniz incelenmeye alındı. Onay sonrası devam edebilirsiniz.",
                            ),
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

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user.role != UserRole.CUSTOMER) {
                        _uiState.update { it.copy(isLoading = false) }
                        sendEffect(
                            LicenseEffect.ShowError("Ehliyet onayı henüz tamamlanmadı."),
                        )
                        return@launch
                    }

                    val session = sessionStore.getSession()
                    if (session == null) {
                        _uiState.update { it.copy(isLoading = false) }
                        sendEffect(LicenseEffect.ShowError("Oturum bulunamadı."))
                        return@launch
                    }

                    sessionStore.saveSession(session.copy(user = user))
                    _uiState.update { it.copy(isLoading = false) }
                    sendEffect(LicenseEffect.NavigateToMain)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    sendEffect(LicenseEffect.ShowError(error.message ?: "Oturum doğrulanamadı."))
                }
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
