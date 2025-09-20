package org.setackle.backend.adapter.outbound.persistence.skill.repository

import org.setackle.backend.adapter.outbound.persistence.skill.entity.ResourceJpaEntity
import org.setackle.backend.domain.skill.model.ResourceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ResourceJpaRepository : JpaRepository<ResourceJpaEntity, Long> {

    fun findByNodeId(nodeId: Long): List<ResourceJpaEntity>

    fun findByResourceType(resourceType: ResourceType): List<ResourceJpaEntity>

    fun findByLanguage(language: String): List<ResourceJpaEntity>

    fun findByIsActiveTrue(): List<ResourceJpaEntity>

    @Query("SELECT r FROM ResourceJpaEntity r WHERE r.nodeId = :nodeId AND r.isActive = true ORDER BY r.displayOrder ASC")
    fun findByNodeIdAndIsActiveTrueOrderByDisplayOrder(@Param("nodeId") nodeId: Long): List<ResourceJpaEntity>

    @Query("SELECT r FROM ResourceJpaEntity r WHERE r.resourceType = :resourceType AND r.isActive = true")
    fun findByResourceTypeAndIsActiveTrue(@Param("resourceType") resourceType: ResourceType): List<ResourceJpaEntity>

    @Query("SELECT r FROM ResourceJpaEntity r WHERE r.language = :language AND r.isActive = true")
    fun findByLanguageAndIsActiveTrue(@Param("language") language: String): List<ResourceJpaEntity>

    @Query("SELECT r FROM ResourceJpaEntity r WHERE r.title LIKE %:keyword% OR r.description LIKE %:keyword%")
    fun findByTitleOrDescriptionContaining(@Param("keyword") keyword: String): List<ResourceJpaEntity>

    @Query("SELECT r FROM ResourceJpaEntity r WHERE r.nodeId = :nodeId AND r.resourceType = :resourceType")
    fun findByNodeIdAndResourceType(@Param("nodeId") nodeId: Long, @Param("resourceType") resourceType: ResourceType): List<ResourceJpaEntity>
}