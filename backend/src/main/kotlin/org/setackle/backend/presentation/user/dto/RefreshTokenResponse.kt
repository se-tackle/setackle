package org.setackle.backend.presentation.user.dto

import org.setackle.backend.application.user.inbound.RefreshTokenResult

/**
 * 토큰 갱신 응답 DTO
 */
data class RefreshTokenResponse(
    val userId: Long,
    val accessToken: String,
    val expiresIn: Long
) {
    companion object {

        /**
         * RefreshTokenResult -> TokenRefreshResponse 변환
         */
        fun from(result: RefreshTokenResult): RefreshTokenResponse {
            return RefreshTokenResponse(
                userId = result.userId,
                accessToken = result.accessToken,
                expiresIn = result.expiresIn
            )
        }
    }
}
