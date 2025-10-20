package org.setackle.backend.domain.assessment.model

import java.time.LocalDateTime

class AssessmentSessionItem(
    val id: Long?,
    val sessionId: Long,
    val questionId: Long,
    var selectedOptionId: Long,
    val questionOrder: Int,
    var isCorrect: Boolean? = null,
    var answeredAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssessmentSessionItem

        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "AssessmentSessionItem(id=$id, isCorrect=$isCorrect, questionId=$questionId, sessionId='$sessionId')"
}
