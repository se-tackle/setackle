package org.setackle.backend.application.user.inbound

import org.setackle.backend.domain.user.vo.RefreshToken

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
)

/**
 * 토큰 갱신 결과
 */
data class RefreshTokenResult(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)
