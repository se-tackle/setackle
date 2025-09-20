package org.setackle.backend.domain.user.model

import java.time.LocalDateTime

class User(
    val id: Long?,
    var email: String,
    var username: String,
    var passwordHash: String,
    var role: UserRole = UserRole.USER,
    var isActive: Boolean = true,
    var emailVerified: Boolean = false,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val deletedAt: LocalDateTime? = null,
    val lastLoginAt: LocalDateTime? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return email == other.email
    }

    override fun hashCode(): Int = email.hashCode()

    override fun toString(): String =
        "User(id=$id, email=$email, username='$username', role=$role, isActive=$isActive)"
}