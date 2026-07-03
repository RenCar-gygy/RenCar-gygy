package com.turkcell.rencarapp.data.auth

data class OtpChallenge(
    val message: String,
    val phone: String,
)

interface AuthRepository {
    suspend fun requestOtp(phone: String): Result<OtpChallenge>

    suspend fun verifyOtp(phone: String, code: String): Result<AuthTokens>

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
    ): Result<AuthTokens>

    suspend fun logout(): Result<Unit>

    suspend fun getCurrentUser(): Result<User>
}

enum class UserRole {
    PENDING,
    CUSTOMER,
    ADMIN,
}

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String?,
    val role: UserRole,
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)
