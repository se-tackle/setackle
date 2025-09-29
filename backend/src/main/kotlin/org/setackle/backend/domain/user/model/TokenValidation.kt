package org.setackle.backend.domain.user.model

import java.time.LocalDateTime

/**
 * Token 검증 결과
 */
data class TokenValidation(
    val isValid: Boolean,
    val userId: Long? = null,
    val email: String? = null,
    val expirationTime: LocalDateTime? = null,
    val reason: String? = null
) {
    companion object {
        fun valid(userId: Long, email: String, expirationTime: LocalDateTime): TokenValidation {
            return TokenValidation(
                isValid = true,
                userId = userId,
                email = email,
                expirationTime = expirationTime
            )
        }

        fun invalid(reason: String): TokenValidation {
            return TokenValidation(
                isValid = false,
                reason = reason
            )
        }
    }
}