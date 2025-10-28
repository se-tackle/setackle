package org.setackle.backend.infrastructure.persistence.skill.repository

import org.setackle.backend.infrastructure.persistence.skill.entity.SkillJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SkillJpaRepository : JpaRepository<SkillJpaEntity, Long> {

    // Skill 관련 쿼리
    fun findByName(name: String): SkillJpaEntity?

    fun existsByName(name: String): Boolean

    fun findByIsActiveTrue(): List<SkillJpaEntity>

    @Query("SELECT s FROM SkillJpaEntity s WHERE s.isActive = true ORDER BY s.displayOrder ASC")
    fun findAllActiveOrderByDisplayOrder(): List<SkillJpaEntity>

    @Query("SELECT s FROM SkillJpaEntity s WHERE s.name LIKE %:keyword% OR s.description LIKE %:keyword%")
    fun findByNameOrDescriptionContaining(keyword: String): List<SkillJpaEntity>

    fun findByDisplayOrderBetween(startOrder: Int, endOrder: Int): List<SkillJpaEntity>

    // Roadmap 관련 쿼리 (slug 기반 조회)
    fun findBySlug(slug: String): SkillJpaEntity?

    fun existsBySlug(slug: String): Boolean

    @Query(
        "SELECT s FROM SkillJpaEntity s WHERE s.isActive = true " +
            "AND s.roadmapType = :roadmapType ORDER BY s.displayOrder ASC",
    )
    fun findAllActiveByRoadmapType(roadmapType: String): List<SkillJpaEntity>

    @Query(
        "SELECT s FROM SkillJpaEntity s WHERE s.isActive = true " +
            "AND s.nodes IS NOT NULL ORDER BY s.displayOrder ASC",
    )
    fun findAllActiveRoadmaps(): List<SkillJpaEntity>
}
