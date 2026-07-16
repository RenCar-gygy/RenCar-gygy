package com.turkcell.rencarapp.data.wallet

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor
import com.turkcell.rencarapp.data.network.api.CardApi
import com.turkcell.rencarapp.data.network.api.WalletApi
import com.turkcell.rencarapp.data.network.dto.CardResponseDto
import com.turkcell.rencarapp.data.network.dto.CreateCardDto
import com.turkcell.rencarapp.data.network.dto.TopupDto
import com.turkcell.rencarapp.data.network.dto.WalletResponseDto
import com.turkcell.rencarapp.data.network.dto.WalletTransactionDto
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultWalletRepository @Inject constructor(
    private val walletApi: WalletApi,
    private val cardApi: CardApi,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : WalletRepository {

    override suspend fun getWallet(): Result<Wallet> =
        authorizedCall { authorization ->
            walletApi.getWallet(authorization = authorization).toDomain()
        }

    override suspend fun topup(amount: Double): Result<Wallet> {
        if (amount !in MIN_TOPUP_AMOUNT..MAX_TOPUP_AMOUNT) {
            return Result.failure(
                IllegalArgumentException("Yükleme tutarı $MIN_TOPUP_AMOUNT–$MAX_TOPUP_AMOUNT TL arasında olmalıdır.")
            )
        }
        return authorizedCall { authorization ->
            walletApi.topup(authorization = authorization, body = TopupDto(amount = amount)).toDomain()
        }
    }

    override suspend fun listCards(): Result<List<SavedCard>> =
        authorizedCall { authorization ->
            cardApi.listCards(authorization = authorization).map { it.toDomain() }
        }

    override suspend fun addCard(
        brand: String,
        last4: String,
        expMonth: Int,
        expYear: Int,
    ): Result<SavedCard> {
        if (last4.length != 4 || !last4.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Kart numarasının son 4 hanesi geçersiz."))
        }
        if (expMonth !in 1..12) {
            return Result.failure(IllegalArgumentException("Son kullanma ayı geçersiz."))
        }
        if (expYear < 2000) {
            return Result.failure(IllegalArgumentException("Son kullanma yılı geçersiz."))
        }
        val normalizedBrand = brand.uppercase()
        if (normalizedBrand !in SUPPORTED_BRANDS) {
            return Result.failure(IllegalArgumentException("Yalnızca VISA veya MASTERCARD desteklenir."))
        }
        return authorizedCall { authorization ->
            cardApi.addCard(
                authorization = authorization,
                body = CreateCardDto(
                    brand = normalizedBrand,
                    last4 = last4,
                    expMonth = expMonth,
                    expYear = expYear,
                ),
            ).toDomain()
        }
    }

    override suspend fun deleteCard(cardId: String): Result<Unit> =
        authorizedCall { authorization ->
            cardApi.deleteCard(authorization = authorization, id = cardId)
        }

    override suspend fun setDefaultCard(cardId: String): Result<SavedCard> =
        authorizedCall { authorization ->
            cardApi.setDefaultCard(authorization = authorization, id = cardId).toDomain()
        }

    private suspend fun <T> authorizedCall(block: suspend (authorization: String) -> T): Result<T> =
        authorizedRequestExecutor.execute(block)

    private fun WalletResponseDto.toDomain(): Wallet =
        Wallet(
            id = id,
            balance = balance,
            transactions = transactions.map { it.toDomain() },
        )

    private fun WalletTransactionDto.toDomain(): WalletTransaction =
        WalletTransaction(
            id = id,
            type = type,
            amount = amount,
            description = description,
            createdAt = Instant.parse(createdAt),
        )

    private fun CardResponseDto.toDomain(): SavedCard =
        SavedCard(
            id = id,
            brand = brand,
            last4 = last4,
            expMonth = expMonth,
            expYear = expYear,
            isDefault = isDefault,
        )

    private companion object {
        const val MIN_TOPUP_AMOUNT = 10.0
        const val MAX_TOPUP_AMOUNT = 5000.0
        val SUPPORTED_BRANDS = setOf("VISA", "MASTERCARD")
    }
}
