package com.turkcell.rencarapp.ui.rental.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RentalSummaryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RentalSummaryUiState())
    val uiState: StateFlow<RentalSummaryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RentalSummaryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: RentalSummaryIntent) {
        when (intent) {
            is RentalSummaryIntent.PayClicked -> {
                viewModelScope.launch {
                    // Ödeme yapıldıktan sonra ana ekrana dön
                    _effect.send(RentalSummaryEffect.NavigateToHome)
                }
            }
            is RentalSummaryIntent.ChangeCardClicked -> {
                viewModelScope.launch {
                    _effect.send(RentalSummaryEffect.ShowToast("Kart değiştirme özelliği henüz aktif değil."))
                }
            }
        }
    }
}