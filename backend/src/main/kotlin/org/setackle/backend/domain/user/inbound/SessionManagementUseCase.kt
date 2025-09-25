package org.setackle.backend.domain.user.inbound

import org.setackle.backend.domain.user.outbound.UserSessionData
import java.time.LocalDateTime

/**
 * 세션 관리 관련 Use Case
 */
interface ManageSessionUseCase {
    fun createSession(
        userId: Long,
        refreshToken: String,
        deviceInfo: String? = null,
        ipAddress: String? = null,
        userAgent: String? = null,
    ): UserSessionData

    fun getActiveSessions(userId: Long): List<UserSessionData>
    fun updateSessionActivity(userId: Long, sessionId: String): Boolean
    fun invalidateSession(userId: Long, sessionId: String, reason: String = "USER_LOGOUT"): Boolean
    fun invalidateAllUserSessions(userId: Long, reason: String = "ADMIN_ACTION"): Int
    fun validateSession(userId: Long, sessionId: String): SessionValidationResult
    fun getSessionSummary(userId: Long): SessionSummary
    fun cleanupInactiveSessions(): Long
}

/**
 * 세션 유효성 검증 결과
 */
data class SessionValidationResult(
    val isValid: Boolean,
    val session: UserSessionData? = null,
    val reason: String? = null,
    val error: String? = null,
)

/**
 * 세션 요약 정보
 */
data class SessionSummary(
    val userId: Long,
    val activeSessionCount: Int,
    val sessions: List<SessionInfo>,
    val lastActivity: LocalDateTime?,
    val error: String? = null,
)

/**
 * 세션 정보
 */
data class SessionInfo(
    val sessionId: String,
    val deviceInfo: String?,
    val ipAddress: String?,
    val loginAt: LocalDateTime,
    val lastActiveAt: LocalDateTime,
    val isCurrentSession: Boolean,
)

/**
 * 세션 관리 관련 예외
 */
class SessionManagementException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
