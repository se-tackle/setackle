package org.setackle.backend.domain.question.model

import java.time.LocalDateTime

class Question(
    val id: Long?,
    val nodeId: Long,
    var content: String,
    var description: String? = null,
    var isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Question

        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "Question(content='$content', description=$description, isActive=$isActive)"
}
