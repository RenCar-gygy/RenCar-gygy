package com.turkcell.rencarapp.data.auth

import com.turkcell.rencarapp.data.network.api.AuthApi
import com.turkcell.rencarapp.data.network.dto.AuthResponseDto
import com.turkcell.rencarapp.data.network.dto.RefreshTokenDto
import com.turkcell.rencarapp.data.network.dto.UserResponseDto
import com.turkcell.rencarapp.data.session.SessionStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bearer gerektiren API çağrılarında access token süresi dolduğunda
 * `POST /auth/refresh` ile oturumu yeniler ve isteği bir kez tekrarlar.
 */
@Singleton
class AuthorizedRequestExecutor @Inject constructor(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
) {
    private val refreshMutex = Mutex()

    suspend fun <T> execute(block: suspend (authorization: String) -> T): Result<T> {
        val session = sessionStore.getSession()
            ?: return Result.failure(IllegalStateException("Oturum bulunamadı."))

        return runAuthorized(block, session.accessToken, session, retryOnUnauthorized = true)
    }

    private suspend fun <T> runAuthorized(
        block: suspend (authorization: String) -> T,
        accessToken: String,
        session: AuthTokens,
        retryOnUnauthorized: Boolean,
    ): Result<T> {
        return try {
            Result.success(block(bearer(accessToken)))
        } catch (exception: HttpException) {
            if (exception.code() == 401 && retryOnUnauthorized) {
                val refreshed = refreshSession(session)
                if (refreshed != null) {
                    runAuthorized(
                        block = block,
                        accessToken = refreshed.accessToken,
                        session = refreshed,
                        retryOnUnauthorized = false,
                    )
                } else {
                    sessionStore.clearSession()
                    Result.failure(IllegalStateException(SESSION_EXPIRED_MESSAGE))
                }
            } else {
                Result.failure(IllegalStateException(httpErrorMessage(exception)))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private suspend fun refreshSession(current: AuthTokens): AuthTokens? =
        refreshMutex.withLock {
            val latest = sessionStore.getSession() ?: return null
            if (latest.accessToken != current.accessToken) {
                return latest
            }
            return try {
                val tokens = authApi
                    .refresh(RefreshTokenDto(refreshToken = latest.refreshToken))
                    .toAuthTokens()
                sessionStore.saveSession(tokens)
                tokens
            } catch (_: Exception) {
                null
            }
        }

    private fun AuthResponseDto.toAuthTokens(): AuthTokens =
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
        when (uppercase()) {
            UserRole.CUSTOMER.name -> UserRole.CUSTOMER
            UserRole.ADMIN.name -> UserRole.ADMIN
            else -> UserRole.PENDING
        }

    private fun bearer(accessToken: String): String = "Bearer $accessToken"

    private fun httpErrorMessage(exception: HttpException): String =
        when (exception.code()) {
            401 -> "Kimlik doğrulama başarısız."
            403 -> "Bu işlem için yetkiniz yok."
            else -> "Sunucu hatası (${exception.code()})."
        }

    private companion object {
        const val SESSION_EXPIRED_MESSAGE = "Oturum süresi doldu. Lütfen tekrar giriş yapın."
    }
}
