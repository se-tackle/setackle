package org.setackle.backend.presentation.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.setackle.backend.application.user.inbound.RegisterUserCommand
import org.setackle.backend.application.user.inbound.RegisterUserResult
import org.setackle.backend.presentation.common.validation.PasswordMatches

/**
 * 회원가입 요청 DTO
 */
@PasswordMatches
data class RegisterRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식을 입력해주세요.")
    val email: String,

    @field:NotBlank(message = "사용자명은 필수입니다.")
    @field:Size(min = 2, max = 30, message = "사용자명은 2자 이상 30자 이하여야 합니다.")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    val password: String,

    @field:NotBlank(message = "비밀번호 확인은 필수입니다.")
    val confirmPassword: String
)

/**
 * RegisterRequest -> RegisterUserCommand 변환
 */
fun RegisterRequest.toCommand(
    deviceInfo: String?,
    ipAddress: String?,
    userAgent: String?,
): RegisterUserCommand {
    return RegisterUserCommand(
        email = this.email,
        username = this.username,
        password = this.password,
        confirmPassword = this.confirmPassword,
        deviceInfo = deviceInfo,
        ipAddress = ipAddress,
        userAgent = userAgent,
    )
}

/**
 * 회원가입 응답 DTO
 */
data class RegisterResponse(
    val userId: Long,
    val email: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String
) {
    companion object {

        /**
         * RegisterUserResult -> RegisterResponse 변환
         */
        fun from(result: RegisterUserResult): RegisterResponse {
            return RegisterResponse(
                userId = result.userId,
                email = result.email,
                username = result.username,
                accessToken = result.accessToken,
                refreshToken = result.refreshToken
            )
        }
    }
}