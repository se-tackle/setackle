package org.setackle.backend.infrastructure.persistence.user

import org.setackle.backend.application.user.outbound.UserPort
import org.setackle.backend.domain.user.model.User
import org.setackle.backend.domain.user.vo.Email
import org.setackle.backend.domain.user.vo.UserId
import org.setackle.backend.domain.user.vo.Username
import org.setackle.backend.infrastructure.persistence.user.entity.UserJpaEntity
import org.setackle.backend.infrastructure.persistence.user.repository.UserJpaRepository
import org.springframework.stereotype.Component

/**
 * 사용자 관련 어댑터 (Port의 구현체)
 * UserJpaRepository를 사용하여 Domain의 UserPort를 구현
 */
@Component
class UserAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserPort {

    override fun findById(id: UserId): User? {
        return userJpaRepository.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByEmail(email: Email): User? {
        return userJpaRepository.findByEmail(email.normalized)?.toDomain()
    }

    override fun findByUsername(username: Username): User? {
        return userJpaRepository.findByUsername(username.normalized)?.toDomain()
    }

    override fun existsByEmail(email: Email): Boolean {
        return userJpaRepository.existsByEmail(email.normalized)
    }

    override fun existsByUsername(username: Username): Boolean {
        return userJpaRepository.existsByUsername(username.normalized)
    }

    override fun create(user: User): User {
        val entity = UserJpaEntity.fromDomain(user)
        val savedEntity = userJpaRepository.save(entity)
        val domainUser = savedEntity.toDomain()

        domainUser.updateIdAfterPersistence(
            UserId.of(savedEntity.id
                ?: throw IllegalArgumentException("Cannot update UserId without ID")
            )
        )

        return domainUser
    }

    override fun update(user: User): User {
        val userId = user.id?.value
            ?: throw IllegalArgumentException("Cannot update user without ID")

        val entity = userJpaRepository.findById(userId)
            .map { existingEntity ->
                existingEntity.apply {
                    email = user.email.normalized
                    username = user.username.normalized
                    passwordHash = user.getHashedPassword()
                    role = user.role
                    isActive = user.isActive
                    emailVerified = user.emailVerified
                    lastLoginAt = user.lastLoginAt
                    deletedAt = user.deletedAt
                    deletionReason = user.deletionReason
                }
            }
            .orElseThrow { IllegalArgumentException("User not found: ${user.id}") }

        val savedEntity = userJpaRepository.save(entity)
        val domainUser = savedEntity.toDomain()

        return domainUser
    }

    override fun delete(id: UserId) {
        userJpaRepository.deleteById(id.value)
    }
}
