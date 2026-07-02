package com.turkcell.rencarapp.data.auth

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    private var currentUser: User? = null

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String?,
    ): Result<AuthTokens> {
        delay(FAKE_DELAY_MS)
        if (email.isBlank() || password.length < 6 || fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçersiz kayıt bilgileri."))
        }
        val user = User(
            id = "fake-user-${email.hashCode()}",
            email = email,
            fullName = fullName,
            phone = phone,
            role = UserRole.PENDING,
        )
        currentUser = user
        return Result.success(fakeTokens(user))
    }

    override suspend fun login(email: String, password: String): Result<AuthTokens> {
        delay(FAKE_DELAY_MS)
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("E-posta ve parola zorunludur."))
        }
        val user = User(
            id = "fake-user-${email.hashCode()}",
            email = email,
            fullName = "Demo Kullanıcı",
            phone = null,
            role = if (email.contains("customer")) UserRole.CUSTOMER else UserRole.PENDING,
        )
        currentUser = user
        return Result.success(fakeTokens(user))
    }

    override suspend fun logout(): Result<Unit> {
        delay(FAKE_DELAY_MS)
        currentUser = null
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): Result<User> {
        delay(FAKE_DELAY_MS)
        return currentUser?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Oturum bulunamadı."))
    }

    private fun fakeTokens(user: User) = AuthTokens(
        accessToken = "fake-access-token",
        refreshToken = "fake-refresh-token",
        user = user,
    )

    private companion object {
        const val FAKE_DELAY_MS = 400L
    }
}
