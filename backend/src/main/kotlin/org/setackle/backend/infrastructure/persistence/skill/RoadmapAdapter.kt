package org.setackle.backend.infrastructure.persistence.skill

import com.fasterxml.jackson.databind.ObjectMapper
import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.setackle.backend.domain.skill.model.Roadmap
import org.setackle.backend.domain.skill.vo.RoadmapSlug
import org.setackle.backend.infrastructure.persistence.skill.entity.SkillJpaEntity
import org.setackle.backend.infrastructure.persistence.skill.repository.SkillJpaRepository
import org.springframework.stereotype.Component

/**
 * 로드맵 관련 어댑터 (Port의 구현체)
 * SkillJpaRepository를 사용하여 RoadmapPort를 구현
 */
@Component
class RoadmapAdapter(
    private val skillJpaRepository: SkillJpaRepository,
    private val objectMapper: ObjectMapper,
) : RoadmapPort {

    override fun findAll(): List<Roadmap> {
        return skillJpaRepository.findAllActiveRoadmaps()
            .map { it.toRoadmap(objectMapper) }
    }

    override fun findBySlug(slug: RoadmapSlug): Roadmap? {
        return skillJpaRepository.findBySlug(slug.value)
            ?.toRoadmap(objectMapper)
    }

    override fun findById(id: Long): Roadmap? {
        return skillJpaRepository.findById(id)
            .map { it.toRoadmap(objectMapper) }
            .orElse(null)
    }

    override fun save(roadmap: Roadmap): Roadmap {
        val entity = if (roadmap.id == null) {
            // 신규 생성
            SkillJpaEntity.fromRoadmap(roadmap, objectMapper)
        } else {
            // 기존 엔티티 업데이트
            skillJpaRepository.findById(roadmap.id)
                .map { existingEntity ->
                    existingEntity.apply {
                        name = roadmap.name
                        description = roadmap.description
                        slug = roadmap.slug
                        roadmapType = roadmap.roadmapType
                        nodes = objectMapper.writeValueAsString(roadmap.nodes)
                        edges = objectMapper.writeValueAsString(roadmap.edges)
                        dimensions = objectMapper.writeValueAsString(roadmap.dimensions)
                        displayOrder = roadmap.displayOrder
                        isActive = roadmap.isActive
                    }
                }
                .orElseThrow { IllegalArgumentException("Roadmap not found: ${roadmap.id}") }
        }

        val savedEntity = skillJpaRepository.save(entity)
        return savedEntity.toRoadmap(objectMapper)
    }

    override fun existsBySlug(slug: RoadmapSlug): Boolean {
        return skillJpaRepository.existsBySlug(slug.value)
    }

    override fun deleteById(id: Long) {
        skillJpaRepository.deleteById(id)
    }
}
