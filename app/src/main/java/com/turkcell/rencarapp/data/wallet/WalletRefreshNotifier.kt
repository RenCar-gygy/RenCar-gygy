package com.turkcell.rencarapp.data.wallet

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** Cüzdan ödemesi sonrası Wallet ekranının bakiyeyi yenilemesi için sinyal. */
@Singleton
class WalletRefreshNotifier @Inject constructor() {

    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    fun notifyWalletChanged() {
        _version.update { it + 1 }
    }
}
