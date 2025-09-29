package org.setackle.backend.adapter.inbound.web.user.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.setackle.backend.adapter.inbound.web.common.ApiResponse
import org.setackle.backend.adapter.inbound.web.user.dto.*
import org.setackle.backend.domain.user.inbound.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val logoutAllSessionsUseCase: LogoutAllSessionsUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
) {

    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody request: RegisterRequest,
        httpRequest: HttpServletRequest
    ): ApiResponse<RegisterResponse> {
        val command = request.toCommand(
            deviceInfo = extractDeviceInfo(httpRequest),
            ipAddress = extractIpAddress(httpRequest),
            userAgent = httpRequest.getHeader("User-Agent")
        )

        val result = registerUserUseCase.register(command)
        val response = RegisterResponse.from(result)

        return ApiResponse.success(response)
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ApiResponse<LoginResponse> {
        val command = request.toCommand(
            deviceInfo = extractDeviceInfo(httpRequest),
            ipAddress = extractIpAddress(httpRequest),
            userAgent = httpRequest.getHeader("User-Agent")
        )

        val result = loginUseCase.login(command)
        val response = LoginResponse.from(result)

        return ApiResponse.success(response)
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 로그아웃합니다.")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    fun logout(
        @AuthenticationPrincipal userId: Long,
        @RequestBody(required = false) request: LogoutRequest?,
        httpRequest: HttpServletRequest
    ): ApiResponse<LogoutResponse> {

        val command = request.toCommand(
            userId = userId,
            reason = "USER_LOGOUT",
            deviceInfo = extractDeviceInfo(httpRequest),
            ipAddress = extractIpAddress(httpRequest),
            userAgent = httpRequest.getHeader("User-Agent")
        )

        val result = logoutUseCase.logout(command)
        val response = LogoutResponse.from(result)

        return ApiResponse.success(response)
    }

    @Operation(summary = "모든 세션 로그아웃", description = "사용자의 모든 활성 세션을 로그아웃합니다.")
    @PostMapping("/logout-all")
    @ResponseStatus(HttpStatus.OK)
    fun logoutAllSessions(
        @AuthenticationPrincipal userId: Long,
        @RequestBody(required = false) request: LogoutAllRequest?,
        httpRequest: HttpServletRequest
    ): ApiResponse<LogoutAllResponse> {

        val command = request.toCommand(
            userId = userId,
            reason = "USER_LOGOUT_ALL"
        )

        val result = logoutAllSessionsUseCase.logoutAllSessions(command)
        val response = LogoutAllResponse.from(result)

        return ApiResponse.success(response)
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest,
        httpRequest: HttpServletRequest
    ): ApiResponse<RefreshTokenResponse> {
        val command = request.toCommand(
            deviceInfo = extractDeviceInfo(httpRequest),
            ipAddress = extractIpAddress(httpRequest),
            userAgent = httpRequest.getHeader("User-Agent")
        )

        val result = refreshTokenUseCase.execute(command)
        val response = RefreshTokenResponse.from(result)

        return ApiResponse.success(response)
    }

    private fun extractDeviceInfo(request: HttpServletRequest): String? {
        val userAgent = request.getHeader("User-Agent") ?: return null
        // 간단한 기기 정보 추출 (실제로는 더 정교한 파싱 필요)
        return when {
            userAgent.contains("Mobile") -> "Mobile"
            userAgent.contains("Tablet") -> "Tablet"
            else -> "Desktop"
        }
    }

    private fun extractIpAddress(request: HttpServletRequest): String? {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        val xRealIp = request.getHeader("X-Real-IP")

        return when {
            !xForwardedFor.isNullOrBlank() -> xForwardedFor.split(",")[0].trim()
            !xRealIp.isNullOrBlank() -> xRealIp
            else -> request.remoteAddr
        }
    }
}
