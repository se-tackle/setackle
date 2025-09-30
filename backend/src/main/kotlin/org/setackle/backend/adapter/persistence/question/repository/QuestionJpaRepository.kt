package org.setackle.backend.adapter.persistence.question.repository

import org.setackle.backend.adapter.persistence.question.entity.QuestionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuestionJpaRepository : JpaRepository<QuestionJpaEntity, Long> {

    fun findByNodeId(nodeId: Long): List<QuestionJpaEntity>

    fun findByIsActiveTrue(): List<QuestionJpaEntity>

    @Query("SELECT q FROM QuestionJpaEntity q WHERE q.nodeId = :nodeId AND q.isActive = true")
    fun findByNodeIdAndIsActiveTrue(@Param("nodeId") nodeId: Long): List<QuestionJpaEntity>

    @Query("SELECT q FROM QuestionJpaEntity q WHERE q.content LIKE %:keyword% OR q.description LIKE %:keyword%")
    fun findByContentOrDescriptionContaining(@Param("keyword") keyword: String): List<QuestionJpaEntity>

    @Query("SELECT q FROM QuestionJpaEntity q WHERE q.nodeId IN :nodeIds AND q.isActive = true")
    fun findByNodeIdInAndIsActiveTrue(@Param("nodeIds") nodeIds: List<Long>): List<QuestionJpaEntity>

    @Query("SELECT COUNT(q) FROM QuestionJpaEntity q WHERE q.nodeId = :nodeId AND q.isActive = true")
    fun countByNodeIdAndIsActiveTrue(@Param("nodeId") nodeId: Long): Long
}