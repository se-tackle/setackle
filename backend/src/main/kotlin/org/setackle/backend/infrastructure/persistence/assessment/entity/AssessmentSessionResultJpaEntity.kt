package org.setackle.backend.infrastructure.persistence.assessment.entity

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.*
import org.setackle.backend.domain.assessment.model.AssessmentSessionResult
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "assessment_session_results")
class AssessmentSessionResultJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "session_id", nullable = false, unique = true, length = 255)
    val sessionId: Long,

    @Column(name = "total_questions", nullable = false)
    val totalQuestions: Int,

    @Column(name = "correct_answers", nullable = false)
    var correctAnswers: Int = 0,

    @Column(name = "total_score", nullable = false, precision = 5, scale = 2)
    var totalScore: BigDecimal = BigDecimal.ZERO,

    @Column(name = "topic_results", columnDefinition = "JSONB")
    var topicResultsJson: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @Transient
    private val objectMapper = jacksonObjectMapper()

    var topicResults: Map<Long, AssessmentSessionResult.TopicResult>
        get() = if (topicResultsJson.isNullOrBlank()) {
            emptyMap()
        } else {
            try {
                objectMapper.readValue(topicResultsJson!!)
            } catch (e: Exception) {
                emptyMap()
            }
        }
        set(value) {
            topicResultsJson = if (value.isEmpty()) {
                null
            } else {
                objectMapper.writeValueAsString(value)
            }
        }

    fun toDomain(): AssessmentSessionResult {
        return AssessmentSessionResult(
            id = id,
            sessionId = sessionId,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            totalScore = totalScore,
            topicResults = topicResults,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(assessmentSessionResult: AssessmentSessionResult): AssessmentSessionResultJpaEntity {
            val entity = AssessmentSessionResultJpaEntity(
                id = assessmentSessionResult.id,
                sessionId = assessmentSessionResult.sessionId,
                totalQuestions = assessmentSessionResult.totalQuestions,
                correctAnswers = assessmentSessionResult.correctAnswers,
                totalScore = assessmentSessionResult.totalScore,
                createdAt = assessmentSessionResult.createdAt ?: LocalDateTime.now(),
                updatedAt = assessmentSessionResult.updatedAt ?: LocalDateTime.now()
            )
            entity.topicResults = assessmentSessionResult.topicResults
            return entity
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}