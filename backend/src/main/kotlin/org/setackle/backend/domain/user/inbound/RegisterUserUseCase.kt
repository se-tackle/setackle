package org.setackle.backend.domain.user.inbound

import org.setackle.backend.domain.user.model.User

/**
 * 사용자 회원가입 유스케이스
 */
interface RegisterUserUseCase {
    fun register(command: RegisterUserCommand): RegisterUserResult
}

/**
 * 회원가입 커맨드
 */
data class RegisterUserCommand(
    val email: String,
    val username: String,
    val password: String,
    val confirmPassword: String,
    val deviceInfo: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null
)

/**
 * 회원가입 결과
 */
data class RegisterUserResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)

