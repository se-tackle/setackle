package org.setackle.backend.domain.model.skill

import java.time.LocalDateTime

class RoadmapNode(
    val id: Long?,
    val skillId: Long,
    val parentId: Long?,
    val nodeType: RoadmapNodeType = RoadmapNodeType.TOPIC,
    var title: String,
    var description: String?,
    var displayOrder: Int = 0,
    var isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoadmapNode

        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "RoadmapNode(id=$id, nodeType=$nodeType, title='$title', description=$description, isActive=$isActive)"
}