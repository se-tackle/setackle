package org.setackle.backend.domain.user.vo

import java.util.*

/**
 * 세션 ID 값 객체
 */
data class SessionId(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "Session ID cannot be blank" }
    }

    companion object {
        fun generate(): SessionId {
            return SessionId(UUID.randomUUID().toString())
        }

        fun of(value: String): SessionId {
            return SessionId(value)
        }
    }
}