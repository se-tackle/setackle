package org.setackle.backend.presentation.user.dto

import org.setackle.backend.application.user.inbound.LogoutAllSessionsCommand
import org.setackle.backend.application.user.inbound.LogoutAllSessionsResult

/**
 * 모든 세션 로그아웃 요청 DTO
 */
data class LogoutAllRequest(
    val currentSessionId: String?,
    val keepCurrentSession: Boolean? = false
)

/**
 * LogoutAllRequest -> LogoutAllSessionsCommand 변환
 */
fun LogoutAllRequest?.toCommand(
    userId: Long,
    reason: String
): LogoutAllSessionsCommand {
    return LogoutAllSessionsCommand(
        userId = userId,
        reason = reason,
        currentSessionId = this?.currentSessionId,
        keepCurrentSession = this?.keepCurrentSession ?: false
    )
}

/**
 * 모든 세션 로그아웃 응답 DTO
 */
data class LogoutAllResponse(
    val success: Boolean,
    val invalidatedSessions: Int,
    val message: String
) {
    companion object {

        /**
         * LogoutAllSessionsResult -> LogoutAllResponse 변환
         */
        fun from(result: LogoutAllSessionsResult): LogoutAllResponse {
            return LogoutAllResponse(
                success = result.success,
                invalidatedSessions = result.invalidatedSessions,
                message = result.message
            )
        }
    }
}
