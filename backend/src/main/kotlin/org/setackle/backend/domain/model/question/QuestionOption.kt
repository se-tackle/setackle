package org.setackle.backend.domain.model.question

import java.time.LocalDateTime

class QuestionOption(
    val id: Long?,
    val questionId: Long,
    var content: String,
    var isCorrect: Boolean = false,
    var orderIndex: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuestionOption

        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "QuestionOption(content='$content', isCorrect=$isCorrect)"
}