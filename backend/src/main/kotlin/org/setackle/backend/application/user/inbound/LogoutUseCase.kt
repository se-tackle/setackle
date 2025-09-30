package org.setackle.backend.application.user.inbound

/**
 * 로그아웃 유스케이스
 */
interface LogoutUseCase {
    fun logout(command: LogoutCommand): LogoutResult
}

/**
 * 로그아웃 커맨드
 */
data class LogoutCommand(
    val userId: Long,
    val sessionId: String? = null,
    val reason: String = "USER_LOGOUT",
    val deviceInfo: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null
)

/**
 * 로그아웃 결과
 */
data class LogoutResult(
    val success: Boolean,
    val message: String
)