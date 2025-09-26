package org.setackle.backend.application.security

import org.setackle.backend.adapter.config.JwtConfig
import org.setackle.backend.domain.user.inbound.*
import org.setackle.backend.domain.user.model.User
import org.setackle.backend.domain.user.model.UserId
import org.setackle.backend.domain.user.outbound.TokenCachePort
import org.setackle.backend.domain.user.outbound.TokenPort
import org.setackle.backend.domain.user.outbound.UserPort
import org.setackle.backend.domain.user.outbound.UserSessionData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class TokenRefreshService(
    private val tokenPort: TokenPort,
    private val tokenCachePort: TokenCachePort,
    private val userPort: UserPort,
    private val tokenBlacklistUseCase: TokenBlacklistUseCase,
    private val jwtConfig: JwtConfig,
) : TokenRefreshUseCase {

    private val logger = LoggerFactory.getLogger(TokenRefreshService::class.java)

    override fun refreshAccessToken(refreshToken: String, deviceInfo: String?): TokenRefreshResult {
        try {
            // 1. Refresh Token 유효성 검증
            if (!tokenPort.validateToken(refreshToken)) {
                logger.warn("유효하지 않은 Refresh Token으로 갱신 시도")
                throw TokenRefreshException("유효하지 않은 Refresh Token입니다")
            }

            // 2. Refresh Token 타입 확인
            if (!tokenPort.isRefreshToken(refreshToken)) {
                logger.warn("Access Token으로 토큰 갱신 시도")
                throw TokenRefreshException("Refresh Token이 아닙니다")
            }

            // 3. 블랙리스트 확인
            if (tokenBlacklistUseCase.isTokenBlacklisted(refreshToken)) {
                logger.warn("블랙리스트에 있는 토큰으로 갱신 시도")
                throw TokenRefreshException("무효화된 토큰입니다")
            }

            // 4. 사용자 정보 추출
            val userId = tokenPort.getUserIdFromToken(refreshToken)
                ?: throw TokenRefreshException("토큰에서 사용자 정보를 찾을 수 없습니다")

            // 5. 저장된 Refresh Token과 비교
            val storedTokenData = tokenCachePort.getRefreshToken(userId)
            if (storedTokenData == null || storedTokenData.token != refreshToken) {
                logger.warn("저장된 Refresh Token과 일치하지 않음: userId=$userId")
                throw TokenRefreshException("유효하지 않은 Refresh Token입니다")
            }

            // 6. 사용자 정보 조회 및 활성 상태 확인
            val user = userPort.findById(UserId.of(userId))
                ?: throw TokenRefreshException("사용자를 찾을 수 없습니다")

            if (!user.isActive || !user.emailVerified) {
                logger.warn("비활성화된 사용자의 토큰 갱신 시도: userId=$userId")
                tokenBlacklistUseCase.blacklistAllUserTokens(userId, "USER_DISABLED")
                throw TokenRefreshException("비활성화된 사용자입니다")
            }

            // 7. 새로운 토큰 발급
            val newAccessToken = tokenPort.generateAccessToken(user)
            val newRefreshToken = generateNewRefreshTokenIfNeeded(refreshToken, user, userId)

            // 8. 세션 정보 업데이트
            updateUserSession(userId, deviceInfo, newRefreshToken ?: refreshToken)

            logger.info("토큰 갱신 성공: userId=$userId, newRefreshToken=${newRefreshToken != null}")

            return TokenRefreshResult(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken ?: refreshToken,
                expiresIn = jwtConfig.accessTokenValidity,
                refreshTokenUpdated = newRefreshToken != null,
            )
        } catch (e: TokenRefreshException) {
            throw e
        } catch (e: Exception) {
            logger.error("토큰 갱신 중 예상치 못한 오류 발생", e)
            throw TokenRefreshException("토큰 갱신에 실패했습니다", e)
        }
    }

    override fun canRefreshToken(refreshToken: String): Boolean {
        return try {
            tokenPort.validateToken(refreshToken) &&
                tokenPort.isRefreshToken(refreshToken) &&
                !tokenBlacklistUseCase.isTokenBlacklisted(refreshToken)
        } catch (e: Exception) {
            logger.debug("토큰 갱신 가능 여부 확인 중 오류: ${e.message}")
            false
        }
    }

    override fun getRefreshTokenExpirationTime(refreshToken: String): LocalDateTime? {
        return try {
            tokenPort.getExpirationFromToken(refreshToken)
        } catch (e: Exception) {
            logger.debug("Refresh Token 만료 시간 확인 중 오류: ${e.message}")
            null
        }
    }

    override fun invalidateAllUserSessions(userId: Long, reason: String) {
        try {
            tokenBlacklistUseCase.blacklistAllUserTokens(userId, reason)
            logger.info("사용자의 모든 세션이 무효화됨: userId=$userId, reason=$reason")
        } catch (e: Exception) {
            logger.error("사용자 세션 무효화 중 오류 발생: userId=$userId", e)
            throw TokenRefreshException("세션 무효화에 실패했습니다", e)
        }
    }

    private fun generateNewRefreshTokenIfNeeded(
        currentRefreshToken: String,
        user: User,
        userId: Long,
    ): String? {
        val expirationTime = tokenPort.getExpirationFromToken(currentRefreshToken)
            ?: return null

        val now = LocalDateTime.now()
        val timeUntilExpiry = Duration.between(now, expirationTime)
        val refreshThreshold = Duration.ofDays(2) // 2일 남았을 때 갱신

        return if (timeUntilExpiry <= refreshThreshold) {
            logger.debug("Refresh Token 만료가 임박하여 새로운 토큰 발급: userId=$userId")

            // 기존 Refresh Token 블랙리스트 추가
            tokenBlacklistUseCase.blacklistToken(currentRefreshToken, "TOKEN_REFRESH")

            // 새로운 Refresh Token 발급 및 저장
            val newRefreshToken = tokenPort.generateRefreshToken(user)
            val refreshTtl = Duration.ofSeconds(jwtConfig.refreshTokenValidity)
            tokenCachePort.saveRefreshToken(userId, newRefreshToken, refreshTtl)

            newRefreshToken
        } else {
            null
        }
    }

    private fun updateUserSession(userId: Long, deviceInfo: String?, refreshTokenId: String) {
        try {
            val existingSession = tokenCachePort.getUserSession(userId)
            val sessionData = existingSession?.copy(
                lastActiveAt = LocalDateTime.now(),
                deviceInfo = deviceInfo ?: existingSession.deviceInfo,
                refreshTokenId = refreshTokenId,
            ) ?: UserSessionData(
                userId = userId,
                sessionId = UUID.randomUUID().toString(),
                deviceInfo = deviceInfo,
                ipAddress = null,
                userAgent = null,
                loginAt = LocalDateTime.now(),
                lastActiveAt = LocalDateTime.now(),
                refreshTokenId = refreshTokenId,
            )

            val sessionTtl = Duration.ofSeconds(jwtConfig.refreshTokenValidity)
            tokenCachePort.saveUserSession(userId, sessionData, sessionTtl)
        } catch (e: Exception) {
            logger.error("사용자 세션 업데이트 중 오류 발생: userId=$userId", e)
        }
    }
}
