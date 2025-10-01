package org.setackle.backend.application.user.service

import org.setackle.backend.application.user.inbound.RegisterUserCommand
import org.setackle.backend.application.user.inbound.RegisterUserResult
import org.setackle.backend.application.user.inbound.RegisterUserUseCase
import org.setackle.backend.application.user.outbound.PasswordEncoderPort
import org.setackle.backend.application.user.outbound.UserPort
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.domain.user.model.User
import org.setackle.backend.domain.user.vo.Email
import org.setackle.backend.domain.user.vo.Password
import org.setackle.backend.domain.user.vo.UserRole
import org.setackle.backend.domain.user.vo.Username
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 회원가입 서비스
 */
@Service
class RegisterUserService(
    private val userPort: UserPort,
    private val passwordEncoder: PasswordEncoderPort,
) : RegisterUserUseCase {

    private val logger = LoggerFactory.getLogger(RegisterUserService::class.java)

    @Transactional
    override fun register(command: RegisterUserCommand): RegisterUserResult {
        val maskedEmail = Email.ofOrNull(command.email)?.masked ?: "[INVALID_EMAIL]"
        logger.info("회원가입 시작: email=$maskedEmail")

        // 1. 값 객체 생성 (검증 포함)
        val email = createValueObject(
            value = command.email,
            errorCode = ErrorCode.INVALID_EMAIL,
            creator = Email::of
        )

        val username = createValueObject(
            value = command.username,
            errorCode = ErrorCode.INVALID_INPUT_VALUE,
            creator = Username::of
        )

        val password = createValueObject(
            value = command.password,
            errorCode = ErrorCode.INVALID_PASSWORD,
        ) {
            Password.of(it, passwordEncoder::encode)
        }

        // 2. 중복 확인
        checkDuplicates(email, username)

        // 3. 도메인 팩토리를 통한 사용자 생성
        val user = User.register(
            email = email,
            username = username,
            password = password,
            role = UserRole.USER,
        )

        // 4. 사용자 저장
        val savedUser = userPort.save(user)
        logger.info("사용자 생성 완료: userId=${savedUser.id}, email=${savedUser.email.masked}")

        val userId = savedUser.id?.value
            ?: throw BusinessException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                mapOf("details" to "사용자 ID가 생성되지 않았습니다.")
            )

        logger.info("회원가입 완료: userId=$userId")

        return RegisterUserResult(
            userId = userId,
            email = savedUser.email.value,
            username = savedUser.username.value,
        )
    }

    /**
     * 값 객체 생성 헬퍼 함수 - 중복 try-catch 제거
     */
    private inline fun <T> createValueObject(
        value: String,
        errorCode: ErrorCode,
        creator: (String) -> T
    ): T = try {
        creator(value)
    } catch (e: IllegalArgumentException) {
        throw BusinessException(errorCode, mapOf("reason" to e.message))
    }

    private fun checkDuplicates(email: Email, username: Username) {
        if (userPort.existsByEmail(email)) {
            throw BusinessException(ErrorCode.DUPLICATE_EMAIL, mapOf("email" to email.normalized))
        }

        if (userPort.existsByUsername(username)) {
            throw BusinessException(ErrorCode.DUPLICATE_USERNAME, mapOf("username" to username.normalized))
        }
    }
}