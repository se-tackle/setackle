package org.setackle.backend.adapter.persistence.skill.entity

import jakarta.persistence.*
import org.setackle.backend.domain.skill.model.Resource
import org.setackle.backend.domain.skill.model.ResourceType
import java.time.LocalDateTime

@Entity
@Table(name = "resources")
class ResourceJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "node_id")
    val nodeId: Long?,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    val resourceType: ResourceType,

    @Column(name = "url", length = 2048)
    var url: String?,

    @Column(name = "language", nullable = false, length = 10)
    var language: String = "ko",

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Resource {
        return Resource(
            id = id,
            nodeId = nodeId,
            title = title,
            description = description,
            resourceType = resourceType,
            url = url,
            language = language,
            displayOrder = displayOrder,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(resource: Resource): ResourceJpaEntity {
            return ResourceJpaEntity(
                id = resource.id,
                nodeId = resource.nodeId,
                title = resource.title,
                description = resource.description,
                resourceType = resource.resourceType,
                url = resource.url,
                language = resource.language,
                displayOrder = resource.displayOrder,
                isActive = resource.isActive,
                createdAt = resource.createdAt ?: LocalDateTime.now(),
                updatedAt = resource.updatedAt ?: LocalDateTime.now()
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}