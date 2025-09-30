package org.setackle.backend.adapter.persistence.user.repository

import org.setackle.backend.adapter.persistence.user.entity.UserJpaEntity
import org.setackle.backend.domain.user.vo.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {

    fun findByEmail(email: String): UserJpaEntity?

    fun findByUsername(username: String): UserJpaEntity?

    fun existsByEmail(email: String): Boolean

    fun existsByUsername(username: String): Boolean

    fun findByIsActiveTrue(): List<UserJpaEntity>

    fun findByRole(role: UserRole): List<UserJpaEntity>

    fun findByEmailVerified(emailVerified: Boolean): List<UserJpaEntity>

    @Query("SELECT u FROM UserJpaEntity u WHERE u.deletedAt IS NULL")
    fun findAllActive(): List<UserJpaEntity>

    @Query("SELECT u FROM UserJpaEntity u WHERE u.lastLoginAt >= :since")
    fun findByLastLoginAtAfter(@Param("since") since: LocalDateTime): List<UserJpaEntity>

    @Query("SELECT u FROM UserJpaEntity u WHERE u.registeredAt BETWEEN :start AND :end")
    fun findByCreatedAtBetween(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<UserJpaEntity>
}