package org.setackle.backend.domain.user.vo

/**
 * Refresh Token 값 객체
 */
data class RefreshToken(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Refresh token value cannot be blank" }
    }
}
