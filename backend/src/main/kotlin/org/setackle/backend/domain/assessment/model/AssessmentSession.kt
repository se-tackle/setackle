package org.setackle.backend.domain.assessment.model

import java.time.LocalDateTime

class AssessmentSession(
    val id: Long?,
    val userId: Long,
    val skillId: Long,
    var totalQuestions: Int,
    var status: SessionStatus = SessionStatus.CREATED,
    var startedAt: LocalDateTime? = null,
    var completedAt: LocalDateTime? = null,
    var expiredAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssessmentSession

        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "AssessmentSession(id=$id, totalQuestions=$totalQuestions, skillId=$skillId, userId=$userId, status=$status)"
}