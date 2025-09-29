package org.setackle.backend.domain.user.inbound

/**
 * 모든 세션 로그아웃 유스케이스
 */
interface LogoutAllSessionsUseCase {
    fun logoutAllSessions(command: LogoutAllSessionsCommand): LogoutAllSessionsResult
}

/**
 * 모든 세션 로그아웃 커맨드
 */
data class LogoutAllSessionsCommand(
    val userId: Long,
    val reason: String = "USER_LOGOUT_ALL",
    val currentSessionId: String? = null, // 현재 세션은 유지할지 여부
    val keepCurrentSession: Boolean = false
)

/**
 * 모든 세션 로그아웃 결과
 */
data class LogoutAllSessionsResult(
    val success: Boolean,
    val invalidatedSessions: Int,
    val message: String
)