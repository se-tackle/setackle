package org.setackle.backend.infrastructure.persistence.skill.entity

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.setackle.backend.domain.skill.model.*
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

    // 로드맵 시각화 관련 필드
    @Column(name = "slug", unique = true)
    var slug: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "roadmap_type")
    var roadmapType: RoadmapType? = RoadmapType.SKILL,

    @Type(JsonBinaryType::class)
    @Column(name = "nodes", columnDefinition = "jsonb")
    var nodes: String? = null,

    @Type(JsonBinaryType::class)
    @Column(name = "edges", columnDefinition = "jsonb")
    var edges: String? = null,

    @Type(JsonBinaryType::class)
    @Column(name = "dimensions", columnDefinition = "jsonb")
    var dimensions: String? = """{"width": 1200, "height": 800}""",
) {
    /**
     * Skill 도메인 모델로 변환
     */
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

    /**
     * Roadmap 도메인 모델로 변환
     * @param objectMapper JSON 직렬화/역직렬화를 위한 ObjectMapper
     * @return Roadmap 도메인 모델
     */
    fun toRoadmap(objectMapper: ObjectMapper): Roadmap {
        val nodesList = nodes?.let {
            objectMapper.readValue(it, object : TypeReference<List<FlowNode>>() {})
        } ?: emptyList()

        val edgesList = edges?.let {
            objectMapper.readValue(it, object : TypeReference<List<FlowEdge>>() {})
        } ?: emptyList()

        val dimensionsObj = dimensions?.let {
            objectMapper.readValue(it, Dimensions::class.java)
        } ?: Dimensions.DEFAULT

        return Roadmap.reconstruct(
            id = id!!,
            name = name,
            description = description,
            slug = slug ?: name.lowercase().replace(" ", "-"),
            roadmapType = roadmapType ?: RoadmapType.SKILL,
            nodes = nodesList,
            edges = edgesList,
            dimensions = dimensionsObj,
            displayOrder = displayOrder,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        /**
         * Skill 도메인에서 JPA Entity로 변환
         */
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

        /**
         * Roadmap 도메인에서 JPA Entity로 변환
         * @param roadmap Roadmap 도메인 모델
         * @param objectMapper JSON 직렬화를 위한 ObjectMapper
         * @return SkillJpaEntity
         */
        fun fromRoadmap(
            roadmap: Roadmap,
            objectMapper: ObjectMapper,
        ): SkillJpaEntity {
            return SkillJpaEntity(
                id = roadmap.id,
                name = roadmap.name,
                description = roadmap.description,
                displayOrder = roadmap.displayOrder,
                isActive = roadmap.isActive,
                createdAt = roadmap.createdAt ?: LocalDateTime.now(),
                updatedAt = roadmap.updatedAt ?: LocalDateTime.now(),
                slug = roadmap.slug,
                roadmapType = roadmap.roadmapType,
                nodes = objectMapper.writeValueAsString(roadmap.nodes),
                edges = objectMapper.writeValueAsString(roadmap.edges),
                dimensions = objectMapper.writeValueAsString(roadmap.dimensions),
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
