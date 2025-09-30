package org.setackle.backend.domain.user.vo

/**
 * Token 쌍 값 객체
 */
data class TokenPair(
    val accessToken: AccessToken,
    val refreshToken: RefreshToken
)