package org.setackle.backend.presentation.user.dto

import org.setackle.backend.application.user.inbound.LogoutResult

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
