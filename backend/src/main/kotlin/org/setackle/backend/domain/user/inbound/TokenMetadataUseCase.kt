package org.setackle.backend.domain.user.inbound

import org.setackle.backend.domain.user.outbound.TokenMetadata
import java.time.Duration
import java.time.LocalDateTime

/**
 * 토큰 메타데이터 추적 관련 Use Case
 */
interface TokenMetadataUseCase {
    fun saveTokenMetadata(
        token: String,
        userId: Long,
        tokenType: String,
        deviceInfo: String? = null,
        ipAddress: String? = null,
    ): String

    fun getTokenMetadata(tokenId: String): TokenMetadata?
    fun getTokenMetadataByToken(token: String): TokenMetadata?
    fun getUserTokenStatistics(userId: Long): UserTokenStatistics
    fun recordTokenIssuance(
        userId: Long,
        tokenType: String,
        deviceInfo: String? = null,
        ipAddress: String? = null,
        userAgent: String? = null,
    ): TokenIssuanceRecord

    fun recordTokenUsage(
        token: String,
        action: String,
        ipAddress: String? = null,
        userAgent: String? = null,
    )

    fun recordSecurityEvent(
        userId: Long?,
        eventType: String,
        severity: String,
        description: String,
        ipAddress: String? = null,
        userAgent: String? = null,
    )

    fun checkTokenExpirationWarning(userId: Long): TokenExpirationWarning?
    fun cleanupExpiredMetadata(): Long
}

/**
 * 사용자 토큰 통계
 */
data class UserTokenStatistics(
    val userId: Long,
    val activeAccessTokens: Int,
    val activeRefreshTokens: Int,
    val totalTokensIssued: Long,
    val lastTokenIssuedAt: LocalDateTime?,
    val lastActivity: LocalDateTime?,
    val error: String? = null,
)

/**
 * 토큰 발급 이력
 */
data class TokenIssuanceRecord(
    val recordId: String,
    val userId: Long,
    val tokenType: String,
    val deviceInfo: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val issuedAt: LocalDateTime,
)

/**
 * 토큰 사용 이력
 */
data class TokenUsageRecord(
    val recordId: String,
    val userId: Long?,
    val tokenType: String,
    val action: String,
    val ipAddress: String?,
    val userAgent: String?,
    val usedAt: LocalDateTime,
)

/**
 * 보안 이벤트
 */
data class SecurityEvent(
    val eventId: String,
    val userId: Long?,
    val eventType: String,
    val severity: String,
    val description: String,
    val ipAddress: String?,
    val userAgent: String?,
    val occurredAt: LocalDateTime,
)

/**
 * 토큰 만료 경고
 */
data class TokenExpirationWarning(
    val userId: Long,
    val tokenType: String,
    val expiresAt: LocalDateTime,
    val timeUntilExpiry: Duration,
    val warningLevel: String,
)

/**
 * 토큰 메타데이터 관련 예외
 */
class TokenMetadataException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
