package org.setackle.backend.adapter.web.user.dto

import jakarta.validation.constraints.NotBlank
import org.setackle.backend.application.user.inbound.RefreshTokenCommand
import org.setackle.backend.application.user.inbound.RefreshTokenResult
import org.setackle.backend.domain.user.vo.RefreshToken
import org.setackle.backend.domain.user.vo.SessionInfo

/**
 * 토큰 갱신 요청 DTO
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh Token은 필수입니다.")
    val refreshToken: String
)

/**
 * RefreshTokenRequest -> RefreshTokenCommand 변환
 */
fun RefreshTokenRequest.toCommand(
    deviceInfo: String?,
    ipAddress: String?,
    userAgent: String?
): RefreshTokenCommand {
    return RefreshTokenCommand(
        refreshToken = RefreshToken(this.refreshToken),
        sessionInfo = SessionInfo.of(deviceInfo, ipAddress, userAgent)
    )
}

/**
 * 토큰 갱신 응답 DTO
 */
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
) {
    companion object {

        /**
         * RefreshTokenResult -> TokenRefreshResponse 변환
         */
        fun from(result: RefreshTokenResult): RefreshTokenResponse {
            return RefreshTokenResponse(
                accessToken = result.tokenPair.accessToken.value,
                refreshToken = result.tokenPair.refreshToken.value,
                expiresIn = result.tokenPair.accessToken.expiresIn
            )
        }
    }
}
