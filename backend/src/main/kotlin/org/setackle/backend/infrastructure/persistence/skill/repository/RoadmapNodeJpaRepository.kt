package org.setackle.backend.infrastructure.persistence.skill.repository

import org.setackle.backend.domain.skill.model.RoadmapNodeType
import org.setackle.backend.infrastructure.persistence.skill.entity.RoadmapNodeJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RoadmapNodeJpaRepository : JpaRepository<RoadmapNodeJpaEntity, Long> {

    fun findBySkillId(skillId: Long): List<RoadmapNodeJpaEntity>

    fun findByParentId(parentId: Long): List<RoadmapNodeJpaEntity>

    fun findByParentIdIsNull(): List<RoadmapNodeJpaEntity>

    fun findByNodeType(nodeType: RoadmapNodeType): List<RoadmapNodeJpaEntity>

    fun findByIsActiveTrue(): List<RoadmapNodeJpaEntity>

    @Query(
        """
        SELECT rn FROM RoadmapNodeJpaEntity rn
        WHERE rn.skillId = :skillId AND rn.isActive = true
        ORDER BY rn.displayOrder ASC
        """,
    )
    fun findBySkillIdAndIsActiveTrueOrderByDisplayOrder(
        @Param("skillId") skillId: Long,
    ): List<RoadmapNodeJpaEntity>

    @Query(
        """
        SELECT rn FROM RoadmapNodeJpaEntity rn
        WHERE rn.parentId = :parentId AND rn.isActive = true
        ORDER BY rn.displayOrder ASC
        """,
    )
    fun findByParentIdAndIsActiveTrueOrderByDisplayOrder(
        @Param("parentId") parentId: Long,
    ): List<RoadmapNodeJpaEntity>

    @Query("SELECT rn FROM RoadmapNodeJpaEntity rn WHERE rn.skillId = :skillId AND rn.nodeType = :nodeType")
    fun findBySkillIdAndNodeType(
        @Param("skillId") skillId: Long,
        @Param("nodeType") nodeType: RoadmapNodeType,
    ): List<RoadmapNodeJpaEntity>

    @Query("SELECT rn FROM RoadmapNodeJpaEntity rn WHERE rn.title LIKE %:keyword% OR rn.description LIKE %:keyword%")
    fun findByTitleOrDescriptionContaining(@Param("keyword") keyword: String): List<RoadmapNodeJpaEntity>
}
