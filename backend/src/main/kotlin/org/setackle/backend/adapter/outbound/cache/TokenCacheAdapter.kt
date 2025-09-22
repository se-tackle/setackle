package org.setackle.backend.adapter.outbound.cache

import org.setackle.backend.domain.user.outbound.RefreshTokenData
import org.setackle.backend.domain.user.outbound.TokenCachePort
import org.setackle.backend.domain.user.outbound.TokenMetadata
import org.setackle.backend.domain.user.outbound.UserSessionData
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * 토큰 캐시 관련 어댑터 (Port의 구현체)
 * TokenCacheRepository를 사용하여 Domain의 TokenCachePort를 구현
 */
@Component
class TokenCacheAdapter(
    private val tokenCacheRepository: TokenCacheRepository,
) : TokenCachePort {

    override fun saveRefreshToken(userId: Long, refreshToken: String, ttl: Duration) {
        tokenCacheRepository.saveRefreshToken(userId, refreshToken, ttl)
    }

    override fun getRefreshToken(userId: Long): RefreshTokenData? {
        return tokenCacheRepository.getRefreshToken(userId)
    }

    override fun deleteRefreshToken(userId: Long) {
        tokenCacheRepository.deleteRefreshToken(userId)
    }

    override fun addToBlacklist(token: String, ttl: Duration) {
        tokenCacheRepository.addToBlacklist(token, ttl)
    }

    override fun isTokenBlacklisted(token: String): Boolean {
        return tokenCacheRepository.isTokenBlacklisted(token)
    }

    override fun saveUserSession(userId: Long, sessionData: UserSessionData, ttl: Duration) {
        tokenCacheRepository.saveUserSession(userId, sessionData, ttl)
    }

    override fun getUserSession(userId: Long): UserSessionData? {
        return tokenCacheRepository.getUserSession(userId)
    }

    override fun deleteUserSession(userId: Long) {
        tokenCacheRepository.deleteUserSession(userId)
    }

    override fun saveTokenMetadata(tokenId: String, metadata: TokenMetadata, ttl: Duration) {
        tokenCacheRepository.saveTokenMetadata(tokenId, metadata, ttl)
    }

    override fun getTokenMetadata(tokenId: String): TokenMetadata? {
        return tokenCacheRepository.getTokenMetadata(tokenId)
    }
}
