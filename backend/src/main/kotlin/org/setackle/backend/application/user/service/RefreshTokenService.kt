package org.setackle.backend.application.user.service

import org.setackle.backend.application.user.inbound.RefreshTokenCommand
import org.setackle.backend.application.user.inbound.RefreshTokenResult
import org.setackle.backend.application.user.inbound.RefreshTokenUseCase
import org.setackle.backend.application.user.outbound.TokenCachePort
import org.setackle.backend.application.user.outbound.TokenPort
import org.setackle.backend.application.user.outbound.UserPort
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.domain.user.model.User
import org.setackle.backend.domain.user.vo.RefreshToken
import org.setackle.backend.domain.user.vo.UserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RefreshTokenService(
    private val tokenPort: TokenPort,
    private val tokenCachePort: TokenCachePort,
    private val userPort: UserPort,
) : RefreshTokenUseCase {

    private val logger = LoggerFactory.getLogger(RefreshTokenService::class.java)

    override fun execute(command: RefreshTokenCommand): RefreshTokenResult {
        val refreshToken = command.refreshToken
//        val sessionInfo = command.sessionInfo

        try {
            // 1. Refresh Token 유효성 검증
            validateRefreshToken(refreshToken)

            // 2. 사용자 정보 추출 및 검증
            val userId = extractAndValidateUser(refreshToken)

            // 3. 저장된 Refresh Token과 비교
            validateStoredToken(refreshToken, userId)

            // 4. 사용자 상태 확인
            val user = validateUserStatus(userId)

            // 5. 새로운 토큰 발급
            val newAccessToken = tokenPort.generateAccessToken(user)
            val newRefreshToken = tokenPort.generateRefreshToken(user)

            val accessTokenValidity = tokenPort.getAccessTokenValiditySeconds()
            val refreshTokenValidity = tokenPort.getRefreshTokenValiditySeconds()

            tokenCachePort.saveRefreshToken(
                userId = userId.value,
                refreshToken = newRefreshToken,
                ttl = Duration.ofSeconds(refreshTokenValidity)
            )

            logger.info("토큰 갱신 성공: userId=${userId.value}")

            return RefreshTokenResult(
                userId = userId.value,
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                expiresIn = accessTokenValidity
            )
        } catch (e: Exception) {
            logger.error("토큰 갱신 중 예상치 못한 오류 발생", e)
            throw BusinessException(ErrorCode.TOKEN_REFRESH_FAILED)
        }
    }

    private fun validateRefreshToken(refreshToken: RefreshToken) {
        if (!tokenPort.validateToken(refreshToken.value)) {
            logger.warn("유효하지 않은 Refresh Token으로 갱신 시도")
            throw BusinessException(ErrorCode.REFRESH_TOKEN_INVALID)
        }

        if (!tokenPort.isRefreshToken(refreshToken.value)) {
            logger.warn("Access Token으로 토큰 갱신 시도")
            throw BusinessException(
                ErrorCode.TOKEN_MALFORMED,
                mapOf(
                    "tokenType" to "ACCESS_TOKEN",
                    "expected" to "REFRESH_TOKEN",
                ),
            )
        }

        if (tokenCachePort.isTokenBlacklisted(refreshToken.value)) {
            logger.warn("블랙리스트에 있는 토큰으로 갱신 시도")
            throw BusinessException(
                ErrorCode.TOKEN_BLACKLISTED,
                mapOf(
                    "tokenType" to "REFRESH_TOKEN",
                    "action" to "REFRESH_ATTEMPT",
                ),
            )
        }
    }

    private fun extractAndValidateUser(refreshToken: RefreshToken): UserId {
        val userId = tokenPort.getUserIdFromToken(refreshToken.value)
            ?: throw BusinessException(
                ErrorCode.TOKEN_INVALID,
                mapOf(
                    "reason" to "USER_ID_NOT_FOUND",
                    "tokenType" to "REFRESH_TOKEN",
                ),
            )
        return UserId.of(userId)
    }

    private fun validateStoredToken(refreshToken: RefreshToken, userId: UserId) {
        val storedToken = tokenCachePort.getRefreshToken(userId.value)
        if (storedToken == null || storedToken != refreshToken.value) {
            logger.warn("저장된 Refresh Token과 일치하지 않음: userId=${userId.value}")
            throw BusinessException(
                ErrorCode.REFRESH_TOKEN_INVALID,
                mapOf(
                    "userId" to userId.value,
                    "reason" to "STORED_TOKEN_MISMATCH",
                ),
            )
        }
    }

    private fun validateUserStatus(userId: UserId): User {
        val user = userPort.findById(userId)
            ?: throw BusinessException(
                ErrorCode.USER_NOT_FOUND,
                mapOf("userId" to userId.value),
            )

        if (!user.isActive) {
            logger.warn("비활성화된 사용자의 토큰 갱신 시도: userId=${userId.value}")
            tokenCachePort.deleteRefreshToken(userId.value)
            throw BusinessException(
                ErrorCode.ACCOUNT_DISABLED,
                mapOf(
                    "userId" to userId.value,
                    "isActive" to user.isActive,
                ),
            )
        }

        return user
    }
}