package org.setackle.backend.domain.skill.model

import java.time.LocalDateTime

class Skill(
    val id: Long?,
    var name: String,
    var description: String,
    var displayOrder: Int = 0,
    var isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Skill

        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "Skill(id=$id, name='$name', description='$description', isActive=$isActive)"
}
