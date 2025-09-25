package org.setackle.backend.application.security

import org.setackle.backend.domain.user.inbound.TokenBlacklistUseCase
import org.setackle.backend.domain.user.inbound.TokenBlacklistException
import org.setackle.backend.domain.user.inbound.TokenBlacklistStatus
import org.setackle.backend.domain.user.outbound.TokenCachePort
import org.setackle.backend.domain.user.outbound.TokenPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class TokenBlacklistService(
    private val tokenCachePort: TokenCachePort,
    private val tokenPort: TokenPort,
) : TokenBlacklistUseCase {

    private val logger = LoggerFactory.getLogger(TokenBlacklistService::class.java)

    override fun blacklistToken(token: String, reason: String) {
        try {
            if (!tokenPort.validateToken(token)) {
                logger.warn("유효하지 않은 토큰을 블랙리스트에 추가 시도: ${token.take(10)}...")
                return
            }

            val expirationTime = tokenPort.getExpirationFromToken(token)
            if (expirationTime == null) {
                logger.warn("토큰 만료 시간을 가져올 수 없음: ${token.take(10)}...")
                return
            }

            val now = LocalDateTime.now()
            if (expirationTime.isBefore(now)) {
                logger.debug("이미 만료된 토큰이므로 블랙리스트 추가 생략: ${token.take(10)}...")
                return
            }

            val ttl = Duration.between(now, expirationTime)
            tokenCachePort.addToBlacklist(token, ttl)

            logger.info("토큰이 블랙리스트에 추가됨 - 사유: $reason, TTL: ${ttl.seconds}초")
        } catch (e: Exception) {
            logger.error("토큰 블랙리스트 추가 중 오류 발생", e)
            throw TokenBlacklistException("토큰을 블랙리스트에 추가할 수 없습니다", e)
        }
    }

    override fun isTokenBlacklisted(token: String): Boolean {
        return try {
            tokenCachePort.isTokenBlacklisted(token)
        } catch (e: Exception) {
            logger.error("토큰 블랙리스트 확인 중 오류 발생", e)
            // 안전을 위해 오류 시 블랙리스트로 간주
            true
        }
    }

    override fun blacklistAllUserTokens(userId: Long, reason: String) {
        try {
            // Refresh Token 삭제
            tokenCachePort.deleteRefreshToken(userId)

            // 사용자 세션 삭제
            tokenCachePort.deleteUserSession(userId)

            logger.info("사용자 $userId 의 모든 토큰이 무효화됨 - 사유: $reason")
        } catch (e: Exception) {
            logger.error("사용자 토큰 일괄 무효화 중 오류 발생", e)
            throw TokenBlacklistException("사용자 토큰을 무효화할 수 없습니다", e)
        }
    }

    override fun blacklistTokenPair(accessToken: String, refreshToken: String, reason: String) {
        try {
            blacklistToken(accessToken, reason)
            blacklistToken(refreshToken, reason)

            val userId = tokenPort.getUserIdFromToken(accessToken)
            userId?.let {
                tokenCachePort.deleteRefreshToken(it)
                tokenCachePort.deleteUserSession(it)
            }

            logger.info("토큰 쌍이 블랙리스트에 추가됨 - 사유: $reason")
        } catch (e: Exception) {
            logger.error("토큰 쌍 블랙리스트 추가 중 오류 발생", e)
            throw TokenBlacklistException("토큰 쌍을 블랙리스트에 추가할 수 없습니다", e)
        }
    }

    override fun getBlacklistStatus(token: String): TokenBlacklistStatus {
        return try {
            val isBlacklisted = isTokenBlacklisted(token)
            val isValid = tokenPort.validateToken(token)
            val userId = tokenPort.getUserIdFromToken(token)
            val expirationTime = tokenPort.getExpirationFromToken(token)

            TokenBlacklistStatus(
                token = token.take(10) + "...",
                isBlacklisted = isBlacklisted,
                isValid = isValid,
                userId = userId,
                expirationTime = expirationTime,
                checkedAt = LocalDateTime.now(),
            )
        } catch (e: Exception) {
            logger.error("토큰 블랙리스트 상태 확인 중 오류 발생", e)
            TokenBlacklistStatus(
                token = token.take(10) + "...",
                isBlacklisted = true,
                isValid = false,
                userId = null,
                expirationTime = null,
                checkedAt = LocalDateTime.now(),
                error = e.message,
            )
        }
    }

    override fun cleanupExpiredBlacklist(): Long {
        return try {
            // Redis TTL에 의해 자동으로 정리되므로 별도 작업 불필요
            logger.debug("만료된 블랙리스트 정리 완료")
            0L
        } catch (e: Exception) {
            logger.error("블랙리스트 정리 중 오류 발생", e)
            0L
        }
    }
}
