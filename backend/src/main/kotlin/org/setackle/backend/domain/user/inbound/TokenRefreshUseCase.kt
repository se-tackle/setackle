package org.setackle.backend.domain.user.inbound

import java.time.LocalDateTime

/**
 * 토큰 갱신 관련 Use Case
 */
interface TokenRefreshUseCase {
    fun refreshAccessToken(refreshToken: String, deviceInfo: String? = null): TokenRefreshResult
    fun canRefreshToken(refreshToken: String): Boolean
    fun getRefreshTokenExpirationTime(refreshToken: String): LocalDateTime?
    fun invalidateAllUserSessions(userId: Long, reason: String = "ADMIN_ACTION")
}

/**
 * 토큰 갱신 결과
 */
data class TokenRefreshResult(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val refreshTokenUpdated: Boolean = false,
)

/**
 * 토큰 갱신 관련 예외
 */
class TokenRefreshException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
