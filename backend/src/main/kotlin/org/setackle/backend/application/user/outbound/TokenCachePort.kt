package org.setackle.backend.application.user.outbound

import java.time.Duration

/**
 * 토큰 캐시 관련 출력 포트
 */
interface TokenCachePort {

    /**
     * Refresh Token 관리
     */
    fun saveRefreshToken(userId: Long, refreshToken: String, ttl: Duration)
    fun getRefreshToken(userId: Long): String?
    fun deleteRefreshToken(userId: Long)

    /**
     * 토큰 블랙리스트 관리
     */
    fun addToBlacklist(token: String, ttl: Duration)
    fun isTokenBlacklisted(token: String): Boolean
}
