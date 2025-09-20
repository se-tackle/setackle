package org.setackle.backend.adapter.outbound.persistence.skill.repository

import org.setackle.backend.adapter.outbound.persistence.skill.entity.SkillJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SkillJpaRepository : JpaRepository<SkillJpaEntity, Long> {

    fun findByName(name: String): SkillJpaEntity?

    fun existsByName(name: String): Boolean

    fun findByIsActiveTrue(): List<SkillJpaEntity>

    @Query("SELECT s FROM SkillJpaEntity s WHERE s.isActive = true ORDER BY s.displayOrder ASC")
    fun findAllActiveOrderByDisplayOrder(): List<SkillJpaEntity>

    @Query("SELECT s FROM SkillJpaEntity s WHERE s.name LIKE %:keyword% OR s.description LIKE %:keyword%")
    fun findByNameOrDescriptionContaining(keyword: String): List<SkillJpaEntity>

    fun findByDisplayOrderBetween(startOrder: Int, endOrder: Int): List<SkillJpaEntity>
}