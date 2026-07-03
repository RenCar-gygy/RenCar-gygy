package com.turkcell.rencarapp.data.auth

import com.turkcell.rencarapp.data.session.SessionStore
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor(
    private val sessionStore: SessionStore,
) : AuthRepository {

    override suspend fun requestOtp(phone: String): Result<OtpChallenge> {
        delay(FAKE_DELAY_MS)
        val normalizedPhone = normalizePhone(phone)
        if (normalizedPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçerli bir telefon numarası girin."))
        }
        if (sessionStore.getRegisteredUser(normalizedPhone) == null) {
            return Result.failure(IllegalStateException("Bu telefon numarasına kayıtlı kullanıcı yok."))
        }
        return Result.success(
            OtpChallenge(
                message = "Doğrulama kodu SMS ile gönderildi (simülasyon).",
                phone = normalizedPhone,
            ),
        )
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<AuthTokens> {
        delay(FAKE_DELAY_MS)
        val normalizedPhone = normalizePhone(phone)
        if (code != FAKE_OTP_CODE) {
            return Result.failure(IllegalArgumentException("Doğrulama kodu geçersiz."))
        }
        val user = sessionStore.getRegisteredUser(normalizedPhone)
            ?: return Result.failure(IllegalStateException("Bu telefon numarasına kayıtlı kullanıcı yok."))
        val tokens = fakeTokens(user)
        sessionStore.saveSession(tokens)
        return Result.success(tokens)
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
    ): Result<AuthTokens> {
        delay(FAKE_DELAY_MS)
        if (email.isBlank() || password.length < 6 || fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçersiz kayıt bilgileri."))
        }
        val normalizedPhone = normalizePhone(phone)
        if (normalizedPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçerli bir telefon numarası girin."))
        }
        if (sessionStore.getRegisteredUser(normalizedPhone) != null) {
            return Result.failure(IllegalStateException("Bu telefon numarası zaten kayıtlı."))
        }
        val user = User(
            id = "fake-user-${normalizedPhone.hashCode()}",
            email = email,
            fullName = fullName,
            phone = normalizedPhone,
            role = UserRole.PENDING,
        )
        val tokens = fakeTokens(user)
        sessionStore.saveSession(tokens)
        return Result.success(tokens)
    }

    override suspend fun logout(): Result<Unit> {
        delay(FAKE_DELAY_MS)
        sessionStore.clearSession()
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): Result<User> {
        delay(FAKE_DELAY_MS)
        return sessionStore.getSession()?.user?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Oturum bulunamadı."))
    }

    private fun fakeTokens(user: User) = AuthTokens(
        accessToken = "fake-access-token-${user.id}",
        refreshToken = "fake-refresh-token-${user.id}",
        user = user,
    )

    private fun normalizePhone(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.length == 10 && digits.startsWith('5') -> "+90$digits"
            digits.length == 12 && digits.startsWith("90") -> "+$digits"
            raw.startsWith("+") && digits.length >= 10 -> raw
            else -> ""
        }
    }

    private companion object {
        const val FAKE_DELAY_MS = 400L
        const val FAKE_OTP_CODE = "123456"
    }
}
