package org.setackle.backend.domain.user.outbound

import java.time.Duration
import java.time.LocalDateTime

/**
 * 토큰 캐시 관련 출력 포트
 */
interface TokenCachePort {

    /**
     * Refresh Token 관리
     */
    fun saveRefreshToken(userId: Long, refreshToken: String, ttl: Duration)
    fun getRefreshToken(userId: Long): RefreshTokenData?
    fun deleteRefreshToken(userId: Long)

    /**
     * 토큰 블랙리스트 관리
     */
    fun addToBlacklist(token: String, ttl: Duration)
    fun isTokenBlacklisted(token: String): Boolean

    /**
     * 사용자 세션 관리
     */
    fun saveUserSession(userId: Long, sessionData: UserSessionData, ttl: Duration)
    fun getUserSession(userId: Long): UserSessionData?
    fun deleteUserSession(userId: Long)

    /**
     * 토큰 메타데이터 관리
     */
    fun saveTokenMetadata(tokenId: String, metadata: TokenMetadata, ttl: Duration)
    fun getTokenMetadata(tokenId: String): TokenMetadata?
}

/**
 * Refresh Token 데이터
 */
data class RefreshTokenData(
    val token: String,
    val userId: Long,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
)

/**
 * 토큰 메타데이터
 */
data class TokenMetadata(
    val tokenId: String,
    val userId: Long,
    val tokenType: String,
    val issuedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val deviceInfo: String? = null,
    val ipAddress: String? = null,
)

/**
 * 사용자 세션 데이터
 */
data class UserSessionData(
    val userId: Long,
    val sessionId: String,
    val deviceInfo: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val loginAt: LocalDateTime,
    val lastActiveAt: LocalDateTime,
    val refreshTokenId: String,
)
