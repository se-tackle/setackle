package org.setackle.backend.infrastructure.persistence.question.repository

import org.setackle.backend.infrastructure.persistence.question.entity.QuestionOptionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuestionOptionJpaRepository : JpaRepository<QuestionOptionJpaEntity, Long> {

    fun findByQuestionId(questionId: Long): List<QuestionOptionJpaEntity>

    fun findByIsCorrectTrue(): List<QuestionOptionJpaEntity>

    @Query("SELECT qo FROM QuestionOptionJpaEntity qo WHERE qo.questionId = :questionId ORDER BY qo.orderIndex ASC")
    fun findByQuestionIdOrderByOrderIndex(@Param("questionId") questionId: Long): List<QuestionOptionJpaEntity>

    @Query("SELECT qo FROM QuestionOptionJpaEntity qo WHERE qo.questionId = :questionId AND qo.isCorrect = true")
    fun findCorrectOptionsByQuestionId(@Param("questionId") questionId: Long): List<QuestionOptionJpaEntity>

    @Query("SELECT qo FROM QuestionOptionJpaEntity qo WHERE qo.questionId IN :questionIds ORDER BY qo.questionId, qo.orderIndex")
    fun findByQuestionIdInOrderByQuestionIdAndOrderIndex(@Param("questionIds") questionIds: List<Long>): List<QuestionOptionJpaEntity>

    @Query("SELECT COUNT(qo) FROM QuestionOptionJpaEntity qo WHERE qo.questionId = :questionId")
    fun countByQuestionId(@Param("questionId") questionId: Long): Long

    @Query("SELECT qo FROM QuestionOptionJpaEntity qo WHERE qo.content LIKE %:keyword%")
    fun findByContentContaining(@Param("keyword") keyword: String): List<QuestionOptionJpaEntity>
}