package org.setackle.backend.domain.user.inbound

import java.time.LocalDateTime

/**
 * 토큰 블랙리스트 관련 Use Case
 */
interface TokenBlacklistUseCase {
    fun blacklistToken(token: String, reason: String = "LOGOUT")
    fun isTokenBlacklisted(token: String): Boolean
    fun blacklistAllUserTokens(userId: Long, reason: String = "FORCE_LOGOUT")
    fun blacklistTokenPair(accessToken: String, refreshToken: String, reason: String = "LOGOUT")
    fun getBlacklistStatus(token: String): TokenBlacklistStatus
    fun cleanupExpiredBlacklist(): Long
}

/**
 * 토큰 블랙리스트 상태 정보
 */
data class TokenBlacklistStatus(
    val token: String,
    val isBlacklisted: Boolean,
    val isValid: Boolean,
    val userId: Long?,
    val expirationTime: LocalDateTime?,
    val checkedAt: LocalDateTime,
    val error: String? = null,
)

/**
 * 토큰 블랙리스트 관련 예외
 */
class TokenBlacklistException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
