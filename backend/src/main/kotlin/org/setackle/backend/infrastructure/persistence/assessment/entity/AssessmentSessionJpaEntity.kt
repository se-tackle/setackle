package org.setackle.backend.infrastructure.persistence.assessment.entity

import jakarta.persistence.*
import org.setackle.backend.domain.assessment.model.AssessmentSession
import org.setackle.backend.domain.assessment.model.SessionStatus
import java.time.LocalDateTime

@Entity
@Table(name = "assessment_sessions")
class AssessmentSessionJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "skill_id", nullable = false)
    val skillId: Long,

    @Column(name = "total_questions", nullable = false)
    var totalQuestions: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: SessionStatus = SessionStatus.CREATED,

    @Column(name = "started_at")
    var startedAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @Column(name = "expired_at")
    var expiredAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): AssessmentSession {
        return AssessmentSession(
            id = id,
            userId = userId,
            skillId = skillId,
            totalQuestions = totalQuestions,
            status = status,
            startedAt = startedAt,
            completedAt = completedAt,
            expiredAt = expiredAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(assessmentSession: AssessmentSession): AssessmentSessionJpaEntity {
            return AssessmentSessionJpaEntity(
                id = assessmentSession.id,
                userId = assessmentSession.userId,
                skillId = assessmentSession.skillId,
                totalQuestions = assessmentSession.totalQuestions,
                status = assessmentSession.status,
                startedAt = assessmentSession.startedAt,
                completedAt = assessmentSession.completedAt,
                expiredAt = assessmentSession.expiredAt,
                createdAt = assessmentSession.createdAt ?: LocalDateTime.now(),
                updatedAt = assessmentSession.updatedAt ?: LocalDateTime.now()
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}