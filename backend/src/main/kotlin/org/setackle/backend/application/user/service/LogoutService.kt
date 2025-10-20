package org.setackle.backend.application.user.service

import org.setackle.backend.application.user.inbound.LogoutCommand
import org.setackle.backend.application.user.inbound.LogoutResult
import org.setackle.backend.application.user.inbound.LogoutUseCase
import org.setackle.backend.application.user.outbound.TokenCachePort
import org.setackle.backend.application.user.outbound.TokenPort
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

/**
 * 로그아웃 서비스
 *
 * 사용자 로그아웃 유스케이스 구현체
 * - Access Token 블랙리스트 추가
 * - Refresh Token 블랙리스트 추가 및 캐시 삭제
 */
@Service
class LogoutService(
    private val tokenCachePort: TokenCachePort,
    private val tokenPort: TokenPort,
) : LogoutUseCase {

    private val logger = LoggerFactory.getLogger(LogoutService::class.java)

    override fun logout(command: LogoutCommand): LogoutResult {
        val userId = command.userId

        logger.info("로그아웃 시도: userId=$userId")

        // 토큰 추출
        val accessToken = tokenPort.resolveToken(command.bearerToken)
            ?: throw BusinessException(ErrorCode.TOKEN_INVALID)

        val refreshToken = tokenCachePort.getRefreshToken(userId)
            ?: throw BusinessException(ErrorCode.TOKEN_INVALID)

        // 토큰 무효화(블랙리스트 추가)
        invalidateTokenPair(
            userId = userId,
            accessToken = accessToken,
            refreshToken = refreshToken
        )

        return LogoutResult(
            success = true,
            message = "로그아웃이 완료되었습니다.",
        )
    }

    /**
     * Access Token과 Refresh Token을 블랙리스트에 추가하고 캐시를 삭제합니다.
     *
     * @throws BusinessException 토큰 무효화 실패 시
     */
    private fun invalidateTokenPair(
        userId: Long,
        accessToken: String,
        refreshToken: String,
    ) {
        logger.info("Token 블랙리스트 추가 시도: userId=$userId")

        addToBlacklist(accessToken)
        addToBlacklist(refreshToken)

        tokenCachePort.deleteRefreshToken(userId)

        logger.info("사용자 $userId 의 모든 토큰이 무효화됨")
    }

    /**
     * 토큰을 블랙리스트에 추가합니다.
     *
     * @param token 블랙리스트에 추가할 토큰
     * @throws BusinessException 토큰 검증 실패 또는 블랙리스트 추가 실패 시
     */
    private fun addToBlacklist(token: String) {
        logger.info("${if (tokenPort.isAccessToken(token)) "Access" else "Refresh"} Token 블랙리스트 추가 시도")

        if (!tokenPort.validateToken(token)) {
            logger.warn("유효하지 않은 토큰을 블랙리스트에 추가 시도: ${token.take(10)}...")
            throw BusinessException(ErrorCode.TOKEN_INVALID)
        }

        val expirationTime = tokenPort.getExpirationFromToken(token)
        if (expirationTime == null) {
            logger.warn("토큰 만료 시간을 가져올 수 없음: ${token.take(10)}...")
            throw BusinessException(ErrorCode.TOKEN_INVALID)
        }

        val now = LocalDateTime.now()
        if (expirationTime.isBefore(now)) {
            logger.debug("이미 만료된 토큰이므로 블랙리스트 추가 생략: ${token.take(10)}...")
            return
        }

        val ttl = Duration.between(now, expirationTime)
        tokenCachePort.addToBlacklist(token, ttl)

        logger.info("토큰이 블랙리스트에 추가됨 - TTL: ${ttl.seconds}초")
    }
}
