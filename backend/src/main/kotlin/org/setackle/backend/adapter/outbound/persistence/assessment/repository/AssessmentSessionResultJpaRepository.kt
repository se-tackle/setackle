package org.setackle.backend.adapter.outbound.persistence.assessment.repository

import org.setackle.backend.adapter.outbound.persistence.assessment.entity.AssessmentSessionResultJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDateTime

interface AssessmentSessionResultJpaRepository : JpaRepository<AssessmentSessionResultJpaEntity, Long> {

    fun findBySessionId(sessionId: Long): AssessmentSessionResultJpaEntity?

    fun existsBySessionId(sessionId: Long): Boolean

    @Query("SELECT asr FROM AssessmentSessionResultJpaEntity asr WHERE asr.totalScore >= :minScore")
    fun findByTotalScoreGreaterThanEqual(@Param("minScore") minScore: BigDecimal): List<AssessmentSessionResultJpaEntity>

    @Query("SELECT asr FROM AssessmentSessionResultJpaEntity asr WHERE asr.correctAnswers >= :minCorrect")
    fun findByCorrectAnswersGreaterThanEqual(@Param("minCorrect") minCorrect: Int): List<AssessmentSessionResultJpaEntity>

    @Query("SELECT asr FROM AssessmentSessionResultJpaEntity asr WHERE asr.createdAt BETWEEN :start AND :end")
    fun findByCreatedAtBetween(@Param("start") start: LocalDateTime, @Param("end") end: LocalDateTime): List<AssessmentSessionResultJpaEntity>

    @Query("SELECT AVG(asr.totalScore) FROM AssessmentSessionResultJpaEntity asr")
    fun findAverageTotalScore(): BigDecimal?

    @Query("SELECT AVG(CAST(asr.correctAnswers AS DOUBLE) / asr.totalQuestions * 100) FROM AssessmentSessionResultJpaEntity asr")
    fun findAverageScorePercentage(): Double?

    @Query("SELECT MAX(asr.totalScore) FROM AssessmentSessionResultJpaEntity asr")
    fun findMaxTotalScore(): BigDecimal?

    @Query("SELECT MIN(asr.totalScore) FROM AssessmentSessionResultJpaEntity asr")
    fun findMinTotalScore(): BigDecimal?

    @Query("SELECT COUNT(asr) FROM AssessmentSessionResultJpaEntity asr WHERE asr.totalScore >= :passingScore")
    fun countPassingResults(@Param("passingScore") passingScore: BigDecimal): Long
}