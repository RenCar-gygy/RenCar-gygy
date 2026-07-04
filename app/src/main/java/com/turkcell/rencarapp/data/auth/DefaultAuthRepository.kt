package com.turkcell.rencarapp.data.auth

import com.turkcell.rencarapp.data.network.api.AuthApi
import com.turkcell.rencarapp.data.network.dto.AuthResponseDto
import com.turkcell.rencarapp.data.network.dto.LoginDto
import com.turkcell.rencarapp.data.network.dto.RegisterDto
import com.turkcell.rencarapp.data.network.dto.UserResponseDto
import com.turkcell.rencarapp.data.network.dto.VerifyOtpDto
import com.turkcell.rencarapp.data.session.SessionStore
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
) : AuthRepository {

    override suspend fun requestOtp(phone: String): Result<OtpChallenge> {
        val normalizedPhone = normalizePhone(phone)
        if (normalizedPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçerli bir telefon numarası girin."))
        }
        return apiCall {
            val response = authApi.login(LoginDto(phone = normalizedPhone))
            OtpChallenge(
                message = response.message,
                phone = response.phone,
            )
        }
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<AuthTokens> {
        val normalizedPhone = normalizePhone(phone)
        if (normalizedPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçerli bir telefon numarası girin."))
        }
        return apiCall {
            val tokens = authApi.verifyOtp(
                VerifyOtpDto(phone = normalizedPhone, code = code),
            ).toDomain()
            sessionStore.saveSession(tokens)
            tokens
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
    ): Result<AuthTokens> {
        val normalizedPhone = normalizePhone(phone)
        if (normalizedPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçerli bir telefon numarası girin."))
        }
        return apiCall {
            val tokens = authApi.register(
                RegisterDto(
                    email = email,
                    password = password,
                    fullName = fullName,
                    phone = normalizedPhone,
                ),
            ).toDomain()
            sessionStore.saveSession(tokens)
            tokens
        }
    }

    override suspend fun logout(): Result<Unit> {
        val session = sessionStore.getSession()
        return apiCall {
            if (session != null) {
                authApi.logout(authorization = bearer(session.accessToken))
            }
            sessionStore.clearSession()
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        val session = sessionStore.getSession()
            ?: return Result.failure(IllegalStateException("Oturum bulunamadı."))
        return apiCall {
            authApi.me(authorization = bearer(session.accessToken)).toDomain()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.success(session.user) },
        )
    }

    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (exception: HttpException) {
            Result.failure(IllegalStateException(httpErrorMessage(exception)))
        } catch (exception: Exception) {
            Result.failure(exception)
        }

    private fun AuthResponseDto.toDomain(): AuthTokens =
        AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = user.toDomain(),
        )

    private fun UserResponseDto.toDomain(): User =
        User(
            id = id,
            email = email,
            fullName = fullName,
            phone = phone,
            role = role.toUserRole(),
        )

    private fun String.toUserRole(): UserRole =
        when (this.uppercase()) {
            UserRole.CUSTOMER.name -> UserRole.CUSTOMER
            UserRole.ADMIN.name -> UserRole.ADMIN
            else -> UserRole.PENDING
        }

    private fun bearer(accessToken: String): String = "Bearer $accessToken"

    private fun httpErrorMessage(exception: HttpException): String =
        when (exception.code()) {
            401 -> "Kimlik doğrulama başarısız."
            409 -> "Bu bilgilerle kayıtlı bir hesap zaten var."
            else -> "Sunucu hatası (${exception.code()})."
        }

    private fun normalizePhone(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.length == 10 && digits.startsWith('5') -> "+90$digits"
            digits.length == 12 && digits.startsWith("90") -> "+$digits"
            raw.startsWith("+") && digits.length >= 10 -> raw
            else -> ""
        }
    }
}
