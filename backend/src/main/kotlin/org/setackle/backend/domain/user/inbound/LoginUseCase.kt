package org.setackle.backend.domain.user.inbound

/**
 * 사용자 로그인 유스케이스
 */
interface LoginUseCase {
    fun login(command: LoginCommand): LoginResult
}

/**
 * 로그인 커맨드
 */
data class LoginCommand(
    val email: String,
    val password: String,
    val deviceInfo: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val rememberMe: Boolean = false
)

/**
 * 로그인 결과
 */
data class LoginResult(
    val userId: Long,
    val email: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val sessionId: String,
    val expiresIn: Long // Access Token 만료 시간 (초)
)