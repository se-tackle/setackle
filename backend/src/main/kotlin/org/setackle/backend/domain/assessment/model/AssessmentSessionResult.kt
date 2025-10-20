package org.setackle.backend.domain.assessment.model

import java.math.BigDecimal
import java.time.LocalDateTime

class AssessmentSessionResult(
    val id: Long?,
    val sessionId: Long,
    val totalQuestions: Int,
    var correctAnswers: Int = 0,
    var totalScore: BigDecimal = BigDecimal.ZERO,
    var topicResults: Map<Long, TopicResult> = emptyMap(), // topicId -> TopicResult
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    data class TopicResult(
        val topicId: Long,
        val topicQuestions: Int,
        val correctAnswers: Int,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssessmentSessionResult

        return sessionId == other.sessionId
    }

    override fun hashCode(): Int = sessionId.hashCode()

    override fun toString(): String =
        "AssessmentSessionResult(sessionId='$sessionId', totalQuestions=$totalQuestions, correctAnswers=$correctAnswers)"
}
