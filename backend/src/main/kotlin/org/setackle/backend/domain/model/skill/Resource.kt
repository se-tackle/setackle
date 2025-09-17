package org.setackle.backend.domain.model.skill

import java.time.LocalDateTime

class Resource(
    val id: Long?,
    val nodeId: Long?,
    var title: String,
    var description: String?,
    val resourceType: ResourceType,
    var url: String?,
    var language: String = "ko",
    var displayOrder: Int = 0,
    var isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resource

        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "Resource(id=$id, title='$title', description=$description, resourceType=$resourceType, isActive=$isActive)"
}