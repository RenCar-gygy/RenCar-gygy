package com.turkcell.rencarapp.data.auth

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

interface AuthRepository {
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String?,
    ): Result<AuthTokens>

    suspend fun login(email: String, password: String): Result<AuthTokens>

    suspend fun logout(): Result<Unit>

    suspend fun getCurrentUser(): Result<User>
}
