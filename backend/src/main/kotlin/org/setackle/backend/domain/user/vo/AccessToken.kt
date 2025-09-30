package org.setackle.backend.domain.user.vo

/**
 * Access Token 값 객체
 */
data class AccessToken(
    val value: String,
    val expiresIn: Long
) {
    init {
        require(value.isNotBlank()) { "Access token value cannot be blank" }
        require(expiresIn > 0) { "Expires in must be positive" }
    }
}