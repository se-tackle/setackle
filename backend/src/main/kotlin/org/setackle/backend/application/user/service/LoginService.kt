package org.setackle.backend.application.user.service

import org.setackle.backend.application.user.inbound.LoginCommand
import org.setackle.backend.application.user.inbound.LoginResult
import org.setackle.backend.application.user.inbound.LoginUseCase
import org.setackle.backend.application.user.outbound.PasswordEncoderPort
import org.setackle.backend.application.user.outbound.TokenCachePort
import org.setackle.backend.application.user.outbound.TokenPort
import org.setackle.backend.application.user.outbound.UserPort
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.domain.user.model.User
import org.setackle.backend.domain.user.vo.Email
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * 로그인 서비스
 *
 * 사용자 로그인 유스케이스 구현체
 * - 로그인 시도 검증
 * - 비밀번호 확인
 */
@Service
class LoginService(
    private val userPort: UserPort,
    private val tokenPort: TokenPort,
    private val tokenCachePort: TokenCachePort,
    private val passwordEncoder: PasswordEncoderPort,
) : LoginUseCase {

    private val logger = LoggerFactory.getLogger(LoginService::class.java)

    override fun login(command: LoginCommand): LoginResult {
        logger.info("로그인 시도: email=${Email.ofOrNull(command.email)?.masked ?: "[INVALID]"}")

        val user = findUserByEmail(command.email)
        validateAccountStatus(user)
        validatePassword(command.password, user.getHashedPassword())

        return processSuccessfulLogin(user)
    }

    private fun findUserByEmail(email: String): User {
        return userPort.findByEmail(Email.of(email))
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }

    private fun validateAccountStatus(user: User) {
        if (!user.isActive) {
            throw BusinessException(ErrorCode.ACCOUNT_DISABLED)
        }
    }

    private fun validatePassword(inputPassword: String, storedPasswordHash: String) {
        if (!passwordEncoder.matches(inputPassword, storedPasswordHash)) {
            throw BusinessException(ErrorCode.INVALID_CREDENTIALS)
        }
    }

    private fun processSuccessfulLogin(user: User): LoginResult {
        val userId = user.id?.value
            ?: throw BusinessException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                mapOf(
                    "id" to user.id.toString(),
                    "details" to "로그인 처리 중 오류 발생",
                ),
            )

        val accessToken = tokenPort.generateAccessToken(user)
        val refreshToken = tokenPort.generateRefreshToken(user)

        val accessTokenValidity = tokenPort.getAccessTokenValiditySeconds()
        val refreshTokenValidity = tokenPort.getRefreshTokenValiditySeconds()

        tokenCachePort.saveRefreshToken(
            userId = userId,
            refreshToken = refreshToken,
            ttl = Duration.ofSeconds(refreshTokenValidity),
        )

        // 마지막 로그인 시간 업데이트
        user.recordLogin()

        logger.info("로그인 성공: userId=$userId")

        return LoginResult(
            userId = userId,
            email = user.email.value,
            username = user.username.value,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = accessTokenValidity,
        )
    }
}
