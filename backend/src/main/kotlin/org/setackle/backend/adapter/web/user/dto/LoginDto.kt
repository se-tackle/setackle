package org.setackle.backend.adapter.web.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.setackle.backend.application.user.inbound.LoginCommand
import org.setackle.backend.application.user.inbound.LoginResult

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식을 입력해주세요.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,

    val rememberMe: Boolean? = false
)

/**
 * LoginRequest -> LoginCommand 변환
 */
fun LoginRequest.toCommand(
    deviceInfo: String?,
    ipAddress: String?,
    userAgent: String?
): LoginCommand {
    return LoginCommand(
        email = this.email,
        password = this.password,
        deviceInfo = deviceInfo,
        ipAddress = ipAddress,
        userAgent = userAgent,
        rememberMe = this.rememberMe ?: false
    )
}

/**
 * 로그인 응답 DTO
 */
data class LoginResponse(
    val userId: Long,
    val email: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val sessionId: String,
    val expiresIn: Long
) {
    companion object {

        /**
         * LoginResult -> LoginResponse 변환
         */
        fun from(result: LoginResult): LoginResponse {
            return LoginResponse(
                userId = result.userId,
                email = result.email,
                username = result.username,
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
                sessionId = result.sessionId,
                expiresIn = result.expiresIn
            )
        }
    }
}
