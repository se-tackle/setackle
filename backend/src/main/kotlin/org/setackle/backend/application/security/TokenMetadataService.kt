package org.setackle.backend.application.security

import org.setackle.backend.adapter.config.JwtConfig
import org.setackle.backend.domain.user.inbound.*
import org.setackle.backend.domain.user.outbound.TokenCachePort
import org.setackle.backend.domain.user.outbound.TokenMetadata
import org.setackle.backend.domain.user.outbound.TokenPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class TokenMetadataService(
    private val tokenCachePort: TokenCachePort,
    private val tokenPort: TokenPort,
    private val jwtConfig: JwtConfig,
) : TokenMetadataUseCase {

    private val logger = LoggerFactory.getLogger(TokenMetadataService::class.java)

    override fun saveTokenMetadata(
        token: String,
        userId: Long,
        tokenType: String,
        deviceInfo: String?,
        ipAddress: String?,
    ): String {
        return try {
            val tokenId = generateTokenId()
            val expirationTime = tokenPort.getExpirationFromToken(token)
                ?: throw TokenMetadataException("토큰 만료 시간을 확인할 수 없습니다")

            val metadata = TokenMetadata(
                tokenId = tokenId,
                userId = userId,
                tokenType = tokenType,
                issuedAt = LocalDateTime.now(),
                expiresAt = expirationTime,
                deviceInfo = deviceInfo,
                ipAddress = ipAddress,
            )

            val ttl = Duration.between(LocalDateTime.now(), expirationTime)
            tokenCachePort.saveTokenMetadata(tokenId, metadata, ttl)

            logger.debug("토큰 메타데이터 저장: tokenId=$tokenId, userId=$userId, type=$tokenType")
            tokenId
        } catch (e: Exception) {
            logger.error("토큰 메타데이터 저장 중 오류 발생", e)
            throw TokenMetadataException("토큰 메타데이터를 저장할 수 없습니다", e)
        }
    }

    override fun getTokenMetadata(tokenId: String): TokenMetadata? {
        return try {
            tokenCachePort.getTokenMetadata(tokenId)
        } catch (e: Exception) {
            logger.error("토큰 메타데이터 조회 중 오류 발생: tokenId=$tokenId", e)
            null
        }
    }

    override fun getTokenMetadataByToken(token: String): TokenMetadata? {
        return try {
            val tokenHash = generateTokenHash(token)
            tokenCachePort.getTokenMetadata(tokenHash)
        } catch (e: Exception) {
            logger.error("토큰 기반 메타데이터 조회 중 오류 발생", e)
            null
        }
    }

    override fun getUserTokenStatistics(userId: Long): UserTokenStatistics {
        return try {
            // 현재 구현에서는 단일 세션만 지원하므로 기본적인 통계만 제공
            val session = tokenCachePort.getUserSession(userId)
            val refreshToken = tokenCachePort.getRefreshToken(userId)

            UserTokenStatistics(
                userId = userId,
                activeAccessTokens = if (session != null) 1 else 0,
                activeRefreshTokens = if (refreshToken != null) 1 else 0,
                totalTokensIssued = 0, // 별도 저장소 필요
                lastTokenIssuedAt = refreshToken?.createdAt,
                lastActivity = session?.lastActiveAt,
            )
        } catch (e: Exception) {
            logger.error("사용자 토큰 통계 조회 중 오류 발생: userId=$userId", e)
            UserTokenStatistics(
                userId = userId,
                activeAccessTokens = 0,
                activeRefreshTokens = 0,
                totalTokensIssued = 0,
                lastTokenIssuedAt = null,
                lastActivity = null,
                error = e.message,
            )
        }
    }

    override fun recordTokenIssuance(
        userId: Long,
        tokenType: String,
        deviceInfo: String?,
        ipAddress: String?,
        userAgent: String?,
    ): TokenIssuanceRecord {
        return try {
            val record = TokenIssuanceRecord(
                recordId = UUID.randomUUID().toString(),
                userId = userId,
                tokenType = tokenType,
                deviceInfo = deviceInfo,
                ipAddress = ipAddress,
                userAgent = userAgent,
                issuedAt = LocalDateTime.now(),
            )

            // 발급 이력은 장기 보관이 필요하므로 별도 저장소 구현 필요
            // 현재는 로그로만 기록
            logger.info("토큰 발급 이력: $record")

            record
        } catch (e: Exception) {
            logger.error("토큰 발급 이력 기록 중 오류 발생", e)
            throw TokenMetadataException("토큰 발급 이력을 기록할 수 없습니다", e)
        }
    }

    override fun recordTokenUsage(
        token: String,
        action: String,
        ipAddress: String?,
        userAgent: String?,
    ) {
        try {
            val userId = tokenPort.getUserIdFromToken(token)
            val tokenType = if (tokenPort.isAccessToken(token)) "ACCESS" else "REFRESH"

            val usageRecord = TokenUsageRecord(
                recordId = UUID.randomUUID().toString(),
                userId = userId,
                tokenType = tokenType,
                action = action,
                ipAddress = ipAddress,
                userAgent = userAgent,
                usedAt = LocalDateTime.now(),
            )

            // 사용 이력은 보안 감사를 위해 별도 저장소 구현 필요
            logger.info("토큰 사용 이력: $usageRecord")
        } catch (e: Exception) {
            logger.error("토큰 사용 이력 기록 중 오류 발생", e)
        }
    }

    override fun recordSecurityEvent(
        userId: Long?,
        eventType: String,
        severity: String,
        description: String,
        ipAddress: String?,
        userAgent: String?,
    ) {
        try {
            val securityEvent = SecurityEvent(
                eventId = UUID.randomUUID().toString(),
                userId = userId,
                eventType = eventType,
                severity = severity,
                description = description,
                ipAddress = ipAddress,
                userAgent = userAgent,
                occurredAt = LocalDateTime.now(),
            )

            // 보안 이벤트는 별도 보안 로그 시스템으로 전송 필요
            logger.warn("보안 이벤트: $securityEvent")
        } catch (e: Exception) {
            logger.error("보안 이벤트 기록 중 오류 발생", e)
        }
    }

    override fun checkTokenExpirationWarning(userId: Long): TokenExpirationWarning? {
        return try {
            val refreshToken = tokenCachePort.getRefreshToken(userId)
            if (refreshToken != null) {
                val now = LocalDateTime.now()
                val timeUntilExpiry = Duration.between(now, refreshToken.expiresAt)
                val warningThreshold = Duration.ofHours(24) // 24시간 전 경고

                if (timeUntilExpiry <= warningThreshold && timeUntilExpiry > Duration.ZERO) {
                    TokenExpirationWarning(
                        userId = userId,
                        tokenType = "REFRESH",
                        expiresAt = refreshToken.expiresAt,
                        timeUntilExpiry = timeUntilExpiry,
                        warningLevel = when {
                            timeUntilExpiry <= Duration.ofHours(1) -> "CRITICAL"
                            timeUntilExpiry <= Duration.ofHours(6) -> "HIGH"
                            else -> "MEDIUM"
                        },
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("토큰 만료 경고 확인 중 오류 발생: userId=$userId", e)
            null
        }
    }

    override fun cleanupExpiredMetadata(): Long {
        // Redis TTL에 의해 자동으로 정리되므로 별도 구현 불필요
        return 0L
    }

    private fun generateTokenId(): String {
        return UUID.randomUUID().toString()
    }

    private fun generateTokenHash(token: String): String {
        return token.hashCode().toString()
    }
}
