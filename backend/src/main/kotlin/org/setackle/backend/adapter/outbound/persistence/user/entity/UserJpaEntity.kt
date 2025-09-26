package org.setackle.backend.adapter.outbound.persistence.user.entity

import jakarta.persistence.*
import org.setackle.backend.domain.user.model.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @Column(name = "username", nullable = false, length = 100)
    var username: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: UserRole = UserRole.USER,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null
) {
    fun toDomain(): User {
        return User.reconstruct(
            id = id?.let { UserId.of(it) },
            email = Email.of(email),
            username = Username.of(username),
            password = Password.fromHash(passwordHash),
            role = role,
            isActive = isActive,
            emailVerified = emailVerified,
            lastLoginAt = lastLoginAt
        )
    }

    companion object {
        fun fromDomain(user: User): UserJpaEntity {
            return UserJpaEntity(
                id = user.id?.value,
                email = user.email.normalized,
                username = user.username.normalized,
                passwordHash = user.getHashedPassword(),
                role = user.role,
                isActive = user.isActive,
                emailVerified = user.emailVerified,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                deletedAt = null,
                lastLoginAt = user.lastLoginAt
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}