package org.setackle.backend.presentation.user.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.setackle.backend.application.user.inbound.*
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.infrastructure.config.CookieProperties
import org.setackle.backend.infrastructure.security.CustomUserDetails
import org.setackle.backend.presentation.common.ApiResponse
import org.setackle.backend.presentation.user.dto.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Duration

@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val cookieProperties: CookieProperties,
) {

    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody request: RegisterRequest,
    ): ApiResponse<RegisterResponse> {
        val command = request.toCommand()
        val result = registerUserUseCase.register(command)
        val response = RegisterResponse.from(result)

        return ApiResponse.success(response)
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ApiResponse<LoginResponse> {
        val command = request.toCommand()
        val result = loginUseCase.login(command)

        // Refresh Token을 HttpOnly Cookie로 설정
        setRefreshTokenCookie(response, result.refreshToken)

        // Response Body에는 Access Token만 포함 (Refresh Token 제거)
        val loginResponse = LoginResponse.from(result)

        return ApiResponse.success(loginResponse)
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃합니다.")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    fun logout(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestHeader(name = "Authorization", required = false) bearerToken: String?,
        httpResponse: HttpServletResponse,
    ): ApiResponse<LogoutResponse> {

        val userId = userDetails.getUserId()
            ?: throw BusinessException(
                ErrorCode.USER_NOT_FOUND,
                mapOf("reason" to "User ID not found in authentication")
            )

        val command = LogoutCommand(
            userId = userId,
            bearerToken = bearerToken,
        )

        val result = logoutUseCase.logout(command)

        // Refresh Token Cookie 삭제
        clearRefreshTokenCookie(httpResponse)

        val logoutResponse = LogoutResponse.from(result)

        return ApiResponse.success(logoutResponse)
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): ApiResponse<RefreshTokenResponse> {
        val command = request.toCommand()

        val result = refreshTokenUseCase.execute(command)
        val response = RefreshTokenResponse.from(result)

        return ApiResponse.success(response)
    }

    /**
     * Refresh Token을 HttpOnly Cookie로 설정
     *
     * @param response HttpServletResponse
     * @param refreshToken Refresh Token 값
     */
    private fun setRefreshTokenCookie(
        response: HttpServletResponse,
        refreshToken: String,
    ) {
        val cookieBuilder = ResponseCookie
            .from("refreshToken", refreshToken)
            .httpOnly(true) // JavaScript 접근 차단 (XSS 방어)
            .secure(cookieProperties.secure) // HTTPS에서만 전송 (환경별 설정)
            .path("/api/auth") // 특정 경로에만 전송
            .maxAge(Duration.ofDays(cookieProperties.maxAgeDays)) // 유효기간
            .sameSite(cookieProperties.sameSite) // CSRF 방어 (Strict/Lax/None)

        // Domain 설정 (프로덕션 환경에서만)
        if (cookieProperties.domain.isNotBlank()) {
            cookieBuilder.domain(cookieProperties.domain)
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString())
    }

    /**
     * Refresh Token Cookie 삭제 (로그아웃 시 사용)
     */
    private fun clearRefreshTokenCookie(response: HttpServletResponse) {
        val cookieBuilder = ResponseCookie
            .from("refreshToken", "")
            .httpOnly(true)
            .secure(cookieProperties.secure)
            .path("/api/auth")
            .maxAge(0) // 즉시 만료
            .sameSite(cookieProperties.sameSite)

        // Domain 설정 (생성 시와 동일하게 설정해야 삭제됨!)
        if (cookieProperties.domain.isNotBlank()) {
            cookieBuilder.domain(cookieProperties.domain)
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString())
    }
}