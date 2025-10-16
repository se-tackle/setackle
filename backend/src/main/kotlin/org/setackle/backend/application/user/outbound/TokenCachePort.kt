package org.setackle.backend.application.user.outbound

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

