package org.setackle.backend.adapter.outbound.persistence.skill.entity

import jakarta.persistence.*
import org.setackle.backend.domain.skill.model.RoadmapNode
import org.setackle.backend.domain.skill.model.RoadmapNodeType
import java.time.LocalDateTime

@Entity
@Table(name = "roadmap_nodes")
class RoadmapNodeJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "skill_id", nullable = false)
    val skillId: Long,

    @Column(name = "parent_id")
    val parentId: Long?,

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false)
    val nodeType: RoadmapNodeType = RoadmapNodeType.TOPIC,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String?,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): RoadmapNode {
        return RoadmapNode(
            id = id,
            skillId = skillId,
            parentId = parentId,
            nodeType = nodeType,
            title = title,
            description = description,
            displayOrder = displayOrder,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(roadmapNode: RoadmapNode): RoadmapNodeJpaEntity {
            return RoadmapNodeJpaEntity(
                id = roadmapNode.id,
                skillId = roadmapNode.skillId,
                parentId = roadmapNode.parentId,
                nodeType = roadmapNode.nodeType,
                title = roadmapNode.title,
                description = roadmapNode.description,
                displayOrder = roadmapNode.displayOrder,
                isActive = roadmapNode.isActive,
                createdAt = roadmapNode.createdAt ?: LocalDateTime.now(),
                updatedAt = roadmapNode.updatedAt ?: LocalDateTime.now()
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}