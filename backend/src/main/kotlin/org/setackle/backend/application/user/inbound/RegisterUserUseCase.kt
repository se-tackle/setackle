package org.setackle.backend.application.user.inbound

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
)

/**
 * 회원가입 결과
 */
data class RegisterUserResult(
    val userId: Long,
    val email: String,
    val username: String,
)

