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
import java.time.LocalDateTime

@Service
class RefreshTokenService(
    private val tokenPort: TokenPort,
    private val tokenCachePort: TokenCachePort,
    private val userPort: UserPort,
) : RefreshTokenUseCase {

    private val logger = LoggerFactory.getLogger(RefreshTokenService::class.java)

    override fun execute(command: RefreshTokenCommand): RefreshTokenResult {
        val refreshToken = command.refreshToken

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

            // 6. 기존 Refresh Token 무효화 (Refresh Token Rotation)
            invalidateOldRefreshToken(refreshToken.value)

            // 7. 새로운 Refresh Token 저장
            tokenCachePort.saveRefreshToken(
                userId = userId.value,
                refreshToken = newRefreshToken,
                ttl = Duration.ofSeconds(tokenPort.getRefreshTokenValiditySeconds()),
            )

            logger.info("토큰 갱신 및 기존 토큰 무효화 완료")

            return RefreshTokenResult(
                userId = userId.value,
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                expiresIn = tokenPort.getAccessTokenValiditySeconds(),
            )
        } catch (e: BusinessException) {
            // BusinessException은 그대로 전파 (구체적인 에러 정보 보존)
            logger.warn("토큰 갱신 실패: ${e.errorCode.code} - ${e.message}")
            throw e
        } catch (e: Exception) {
            // 예상치 못한 시스템 오류만 일반화
            logger.error("토큰 갱신 중 예상치 못한 오류 발생", e)
            throw BusinessException(ErrorCode.TOKEN_REFRESH_FAILED)
        }
    }

    private fun validateRefreshToken(refreshToken: RefreshToken) {
        if (!tokenPort.validateToken(refreshToken.value)) {
            logger.warn("유효하지 않은 Refresh Token으로 갱신 시도")
            throw BusinessException(
                ErrorCode.REFRESH_TOKEN_INVALID,
                mapOf("reason" to "TOKEN_VALIDATION_FAILED"),
            )
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

        when {
            storedToken == null -> {
                logger.warn("저장된 Refresh Token이 존재하지 않음")
                throw BusinessException(
                    ErrorCode.REFRESH_TOKEN_INVALID,
                    mapOf("reason" to "TOKEN_NOT_FOUND"),
                )
            }
            storedToken != refreshToken.value -> {
                logger.warn("저장된 Refresh Token과 일치하지 않음")
                throw BusinessException(
                    ErrorCode.REFRESH_TOKEN_INVALID,
                    mapOf("reason" to "TOKEN_MISMATCH"),
                )
            }
        }
    }

    private fun validateUserStatus(userId: UserId): User {
        val user = userPort.findById(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        if (!user.isActive) {
            logger.warn("비활성화된 사용자의 토큰 갱신 시도")
            tokenCachePort.deleteRefreshToken(userId.value)
            throw BusinessException(ErrorCode.ACCOUNT_DISABLED)
        }

        return user
    }

    /**
     * 기존 Refresh Token을 블랙리스트에 추가하여 무효화합니다.
     * Refresh Token Rotation 패턴 구현
     */
    private fun invalidateOldRefreshToken(oldRefreshToken: String) {
        val expirationTime = tokenPort.getExpirationFromToken(oldRefreshToken)
            ?: run {
                logger.warn("기존 Refresh Token의 만료 시간을 가져올 수 없음")
                return
            }

        val now = LocalDateTime.now()
        if (expirationTime.isBefore(now)) {
            logger.debug("기존 Refresh Token이 이미 만료되어 블랙리스트 추가 생략")
            return
        }

        val ttl = Duration.between(now, expirationTime)
        tokenCachePort.addToBlacklist(oldRefreshToken, ttl)

        logger.info("기존 Refresh Token이 블랙리스트에 추가됨 - TTL: ${ttl.seconds}초")
    }
}
