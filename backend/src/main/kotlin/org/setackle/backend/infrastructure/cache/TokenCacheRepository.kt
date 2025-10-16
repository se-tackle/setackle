package org.setackle.backend.infrastructure.cache

import com.fasterxml.jackson.databind.ObjectMapper
import org.setackle.backend.application.user.outbound.RefreshTokenData
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDateTime

@Repository
class TokenCacheRepository(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private const val REFRESH_TOKEN_PREFIX = "refresh_token:"
        private const val TOKEN_BLACKLIST_PREFIX = "blacklist:"
    }

    /**
     * Refresh Token 저장
     */
    fun saveRefreshToken(userId: Long, refreshToken: String, ttl: Duration) {
        val key = "${REFRESH_TOKEN_PREFIX}$userId"
        val tokenData = RefreshTokenData(
            token = refreshToken,
            userId = userId,
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusSeconds(ttl.seconds),
        )

        redisTemplate.opsForValue().set(key, tokenData, ttl)
    }

    /**
     * 사용자 ID로 Refresh Token 조회
     */
    fun getRefreshToken(userId: Long): RefreshTokenData? {
        val key = "${REFRESH_TOKEN_PREFIX}$userId"
        return redisTemplate.opsForValue().get(key)?.let {
            objectMapper.convertValue(it, RefreshTokenData::class.java)
        }
    }

    /**
     * Refresh Token 삭제 (로그아웃)
     */
    fun deleteRefreshToken(userId: Long) {
        val key = "${REFRESH_TOKEN_PREFIX}$userId"
        redisTemplate.delete(key)
    }

    /**
     * 토큰을 블랙리스트에 추가
     */
    fun addToBlacklist(token: String, ttl: Duration) {
        val key = "${TOKEN_BLACKLIST_PREFIX}${token.hashCode()}"
        val blacklistData = TokenBlacklistData(
            tokenHash = token.hashCode().toString(),
            blacklistedAt = LocalDateTime.now(),
            reason = "LOGOUT",
        )

        redisTemplate.opsForValue().set(key, blacklistData, ttl)
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    fun isTokenBlacklisted(token: String): Boolean {
        val key = "${TOKEN_BLACKLIST_PREFIX}${token.hashCode()}"
        return redisTemplate.hasKey(key)
    }

    /**
     * 특정 패턴의 키들 삭제 (관리자 기능)
     */
    fun deleteKeysByPattern(pattern: String): Long {
        val keys = redisTemplate.keys(pattern)
        return if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
        } else {
            0L
        }
    }
}

/**
 * 토큰 블랙리스트 데이터 클래스
 */
data class TokenBlacklistData(
    val tokenHash: String,
    val blacklistedAt: LocalDateTime,
    val reason: String = "LOGOUT",
)
