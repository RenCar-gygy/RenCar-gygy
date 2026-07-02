package com.turkcell.rencarapp.ui.rental.active

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveRentalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rentalId: String = checkNotNull(savedStateHandle["rentalId"])

    private val _uiState = MutableStateFlow(ActiveRentalUiState(rentalId = rentalId))
    val uiState: StateFlow<ActiveRentalUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ActiveRentalEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            is ActiveRentalIntent.LoadRental -> {
                // In a real app, fetch rental data
            }
            is ActiveRentalIntent.ToggleLock -> {
                _uiState.update { it.copy(isLocked = !it.isLocked) }
                viewModelScope.launch {
                    val msg = if (_uiState.value.isLocked) "Araç kilitlendi" else "Araç kilidi açıldı"
                    _effect.send(ActiveRentalEffect.ShowMessage(msg))
                }
            }
            is ActiveRentalIntent.FinishRental -> {
                viewModelScope.launch {
                    _effect.send(ActiveRentalEffect.NavigateToMain)
                }
            }
        }
    }
}
