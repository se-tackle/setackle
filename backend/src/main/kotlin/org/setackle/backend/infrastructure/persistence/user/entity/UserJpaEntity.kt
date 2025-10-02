package org.setackle.backend.infrastructure.persistence.user.entity

import jakarta.persistence.*
import org.setackle.backend.domain.user.model.User
import org.setackle.backend.domain.user.vo.*
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

    @Column(name = "registered_at", nullable = false, updatable = false)
    val registeredAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    @Column(name = "deletion_reason")
    var deletionReason: String? = null,
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
            registeredAt = registeredAt,
            lastLoginAt = lastLoginAt,
            deletedAt = deletedAt,
            deletionReason = deletionReason,
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
                registeredAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                lastLoginAt = user.lastLoginAt,
                deletedAt = user.deletedAt,
                deletionReason = user.deletionReason,
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}