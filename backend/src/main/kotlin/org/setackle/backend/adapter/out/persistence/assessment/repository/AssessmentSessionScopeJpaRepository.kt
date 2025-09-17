package org.setackle.backend.adapter.out.persistence.assessment.repository

import org.setackle.backend.adapter.out.persistence.assessment.entity.AssessmentSessionScopeId
import org.setackle.backend.adapter.out.persistence.assessment.entity.AssessmentSessionScopeJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AssessmentSessionScopeJpaRepository : JpaRepository<AssessmentSessionScopeJpaEntity, AssessmentSessionScopeId> {

    fun findBySessionId(sessionId: Long): List<AssessmentSessionScopeJpaEntity>

    fun findByNodeId(nodeId: Long): List<AssessmentSessionScopeJpaEntity>

    @Query("SELECT ss.nodeId FROM AssessmentSessionScopeJpaEntity ss WHERE ss.sessionId = :sessionId")
    fun findNodeIdsBySessionId(@Param("sessionId") sessionId: Long): List<Long>

    @Query("SELECT ss.sessionId FROM AssessmentSessionScopeJpaEntity ss WHERE ss.nodeId = :nodeId")
    fun findSessionIdsByNodeId(@Param("nodeId") nodeId: Long): List<Long>

    @Query("SELECT COUNT(ss) FROM AssessmentSessionScopeJpaEntity ss WHERE ss.sessionId = :sessionId")
    fun countBySessionId(@Param("sessionId") sessionId: Long): Long

    fun deleteBySessionId(sessionId: Long)

    fun deleteByNodeId(nodeId: Long)
}