package org.setackle.backend.domain.user.inbound

import org.setackle.backend.domain.user.model.RefreshToken
import org.setackle.backend.domain.user.model.SessionInfo
import org.setackle.backend.domain.user.model.TokenPair

/**
 * Access Token 갱신 유스케이스
 */
interface RefreshTokenUseCase {
    fun execute(command: RefreshTokenCommand): RefreshTokenResult
}

/**
 * 토큰 갱신 커맨드
 */
data class RefreshTokenCommand(
    val refreshToken: RefreshToken,
    val sessionInfo: SessionInfo
)

/**
 * 토큰 갱신 결과
 */
data class RefreshTokenResult(
    val tokenPair: TokenPair,
    val refreshTokenUpdated: Boolean = false
)