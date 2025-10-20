package org.setackle.backend.infrastructure.persistence.skill.entity

import jakarta.persistence.*
import org.setackle.backend.domain.skill.model.Skill
import java.time.LocalDateTime

@Entity
@Table(name = "skills")
class SkillJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false, unique = true)
    var name: String,

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): Skill {
        return Skill(
            id = id,
            name = name,
            description = description,
            displayOrder = displayOrder,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun fromDomain(skill: Skill): SkillJpaEntity {
            return SkillJpaEntity(
                id = skill.id,
                name = skill.name,
                description = skill.description,
                displayOrder = skill.displayOrder,
                isActive = skill.isActive,
                createdAt = skill.createdAt ?: LocalDateTime.now(),
                updatedAt = skill.updatedAt ?: LocalDateTime.now(),
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
