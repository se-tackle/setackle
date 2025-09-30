package org.setackle.backend.adapter.web.user.dto

import org.setackle.backend.application.user.inbound.LogoutCommand
import org.setackle.backend.application.user.inbound.LogoutResult

/**
 * 로그아웃 요청 DTO
 */
data class LogoutRequest(
    val sessionId: String?
)

/**
 * LogoutRequest -> LogoutCommand 변환
 */
fun LogoutRequest?.toCommand(
    userId: Long,
    reason: String,
    deviceInfo: String?,
    ipAddress: String?,
    userAgent: String?
): LogoutCommand {
    return LogoutCommand(
        userId = userId,
        sessionId = this?.sessionId,
        reason = reason,
        deviceInfo = deviceInfo,
        ipAddress = ipAddress,
        userAgent = userAgent
    )
}

/**
 * 로그아웃 응답 DTO
 */
data class LogoutResponse(
    val success: Boolean,
    val message: String
) {
    companion object {

        /**
         * LogoutResult -> LogoutResponse 변환
         */
        fun from(result: LogoutResult): LogoutResponse {
            return LogoutResponse(
                success = result.success,
                message = result.message
            )
        }
    }
}
