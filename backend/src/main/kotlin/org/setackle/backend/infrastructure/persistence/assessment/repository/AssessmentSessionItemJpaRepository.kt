package org.setackle.backend.infrastructure.persistence.assessment.repository

import org.setackle.backend.infrastructure.persistence.assessment.entity.AssessmentSessionItemJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AssessmentSessionItemJpaRepository : JpaRepository<AssessmentSessionItemJpaEntity, Long> {

    fun findBySessionId(sessionId: Long): List<AssessmentSessionItemJpaEntity>

    fun findByQuestionId(questionId: Long): List<AssessmentSessionItemJpaEntity>

    @Query(
        """
        SELECT asi FROM AssessmentSessionItemJpaEntity asi
        WHERE asi.sessionId = :sessionId
        ORDER BY asi.questionOrder ASC
        """,
    )
    fun findBySessionIdOrderByQuestionOrder(
        @Param("sessionId") sessionId: Long,
    ): List<AssessmentSessionItemJpaEntity>

    @Query(
        "SELECT asi FROM AssessmentSessionItemJpaEntity asi WHERE asi.sessionId = :sessionId AND asi.isCorrect = true",
    )
    fun findCorrectAnswersBySessionId(@Param("sessionId") sessionId: Long): List<AssessmentSessionItemJpaEntity>

    @Query(
        "SELECT asi FROM AssessmentSessionItemJpaEntity asi WHERE asi.sessionId = :sessionId AND asi.isCorrect = false",
    )
    fun findIncorrectAnswersBySessionId(@Param("sessionId") sessionId: Long): List<AssessmentSessionItemJpaEntity>

    @Query(
        """
        SELECT asi FROM AssessmentSessionItemJpaEntity asi
        WHERE asi.sessionId = :sessionId AND asi.answeredAt IS NOT NULL
        """,
    )
    fun findAnsweredItemsBySessionId(
        @Param("sessionId") sessionId: Long,
    ): List<AssessmentSessionItemJpaEntity>

    @Query("SELECT COUNT(asi) FROM AssessmentSessionItemJpaEntity asi WHERE asi.sessionId = :sessionId")
    fun countBySessionId(@Param("sessionId") sessionId: Long): Long

    @Query(
        """
        SELECT COUNT(asi) FROM AssessmentSessionItemJpaEntity asi
        WHERE asi.sessionId = :sessionId AND asi.isCorrect = true
        """,
    )
    fun countCorrectAnswersBySessionId(
        @Param("sessionId") sessionId: Long,
    ): Long

    @Query(
        """
        SELECT COUNT(asi) FROM AssessmentSessionItemJpaEntity asi
        WHERE asi.sessionId = :sessionId AND asi.answeredAt IS NOT NULL
        """,
    )
    fun countAnsweredItemsBySessionId(
        @Param("sessionId") sessionId: Long,
    ): Long

    fun deleteBySessionId(sessionId: Long)
}
