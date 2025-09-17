package org.setackle.backend.adapter.out.persistence.assessment.repository

import org.setackle.backend.adapter.out.persistence.assessment.entity.AssessmentSessionJpaEntity
import org.setackle.backend.domain.model.assessment.SessionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface AssessmentSessionJpaRepository : JpaRepository<AssessmentSessionJpaEntity, Long> {

    fun findByUserId(userId: Long): List<AssessmentSessionJpaEntity>

    fun findBySkillId(skillId: Long): List<AssessmentSessionJpaEntity>

    fun findByStatus(status: SessionStatus): List<AssessmentSessionJpaEntity>

    @Query("SELECT a FROM AssessmentSessionJpaEntity a WHERE a.userId = :userId AND a.skillId = :skillId")
    fun findByUserIdAndSkillId(@Param("userId") userId: Long, @Param("skillId") skillId: Long): List<AssessmentSessionJpaEntity>

    @Query("SELECT a FROM AssessmentSessionJpaEntity a WHERE a.userId = :userId AND a.status = :status")
    fun findByUserIdAndStatus(@Param("userId") userId: Long, @Param("status") status: SessionStatus): List<AssessmentSessionJpaEntity>

    @Query("SELECT a FROM AssessmentSessionJpaEntity a WHERE a.status IN :statuses")
    fun findByStatusIn(@Param("statuses") statuses: List<SessionStatus>): List<AssessmentSessionJpaEntity>

    @Query("SELECT a FROM AssessmentSessionJpaEntity a WHERE a.expiredAt < :now AND a.status IN ('CREATED', 'IN_PROGRESS', 'PAUSED')")
    fun findExpiredActiveSessions(@Param("now") now: LocalDateTime): List<AssessmentSessionJpaEntity>

    @Query("SELECT a FROM AssessmentSessionJpaEntity a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    fun findByUserIdOrderByCreatedAtDesc(@Param("userId") userId: Long): List<AssessmentSessionJpaEntity>

    @Query("SELECT a FROM AssessmentSessionJpaEntity a WHERE a.createdAt BETWEEN :start AND :end")
    fun findByCreatedAtBetween(@Param("start") start: LocalDateTime, @Param("end") end: LocalDateTime): List<AssessmentSessionJpaEntity>

    @Query("SELECT COUNT(a) FROM AssessmentSessionJpaEntity a WHERE a.userId = :userId AND a.status = 'COMPLETED'")
    fun countCompletedSessionsByUserId(@Param("userId") userId: Long): Long
}