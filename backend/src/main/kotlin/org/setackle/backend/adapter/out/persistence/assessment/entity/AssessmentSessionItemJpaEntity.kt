package org.setackle.backend.adapter.out.persistence.assessment.entity

import jakarta.persistence.*
import org.setackle.backend.domain.model.assessment.AssessmentSessionItem
import java.time.LocalDateTime

@Entity
@Table(name = "assessment_session_items")
class AssessmentSessionItemJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "session_id", nullable = false, length = 255)
    val sessionId: Long,

    @Column(name = "question_id", nullable = false)
    val questionId: Long,

    @Column(name = "selected_option_id", nullable = false)
    var selectedOptionId: Long,

    @Column(name = "question_order", nullable = false)
    val questionOrder: Int,

    @Column(name = "is_correct")
    var isCorrect: Boolean? = null,

    @Column(name = "answered_at")
    var answeredAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): AssessmentSessionItem {
        return AssessmentSessionItem(
            id = id,
            sessionId = sessionId,
            questionId = questionId,
            selectedOptionId = selectedOptionId,
            questionOrder = questionOrder,
            isCorrect = isCorrect,
            answeredAt = answeredAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(assessmentSessionItem: AssessmentSessionItem): AssessmentSessionItemJpaEntity {
            return AssessmentSessionItemJpaEntity(
                id = assessmentSessionItem.id,
                sessionId = assessmentSessionItem.sessionId,
                questionId = assessmentSessionItem.questionId,
                selectedOptionId = assessmentSessionItem.selectedOptionId,
                questionOrder = assessmentSessionItem.questionOrder,
                isCorrect = assessmentSessionItem.isCorrect,
                answeredAt = assessmentSessionItem.answeredAt,
                createdAt = assessmentSessionItem.createdAt ?: LocalDateTime.now(),
                updatedAt = assessmentSessionItem.updatedAt ?: LocalDateTime.now()
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}