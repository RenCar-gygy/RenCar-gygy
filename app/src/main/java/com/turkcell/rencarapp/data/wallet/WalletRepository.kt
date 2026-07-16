package com.turkcell.rencarapp.data.wallet

import java.time.Instant

data class Wallet(
    val id: String,
    val balance: Double,
    val transactions: List<WalletTransaction>,
)

data class WalletTransaction(
    val id: String,
    val type: String,
    val amount: Double,
    val description: String,
    val createdAt: Instant,
)

data class SavedCard(
    val id: String,
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val isDefault: Boolean,
)

interface WalletRepository {
    suspend fun getWallet(): Result<Wallet>

    suspend fun topup(amount: Double): Result<Wallet>

    suspend fun listCards(): Result<List<SavedCard>>

    suspend fun addCard(
        brand: String,
        last4: String,
        expMonth: Int,
        expYear: Int,
    ): Result<SavedCard>

    suspend fun deleteCard(cardId: String): Result<Unit>

    suspend fun setDefaultCard(cardId: String): Result<SavedCard>
}
