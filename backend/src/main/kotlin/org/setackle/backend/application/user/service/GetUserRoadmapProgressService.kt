package org.setackle.backend.application.user.service

import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.setackle.backend.application.user.inbound.GetUserRoadmapProgressUseCase
import org.setackle.backend.application.user.inbound.UserProgressResult
import org.setackle.backend.application.user.inbound.UserRoadmapSummary
import org.setackle.backend.application.user.outbound.RoadmapProgressPort
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.domain.skill.model.Roadmap
import org.setackle.backend.domain.skill.vo.RoadmapSlug
import org.setackle.backend.domain.user.model.UserRoadmapProgress
import org.setackle.backend.domain.user.vo.UserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

/**
 * 사용자 로드맵 진행도 조회 서비스
 *
 * 사용자의 로드맵 학습 진행 상황을 조회합니다.
 */
@Service
@Transactional(readOnly = true)
class GetUserRoadmapProgressService(
    private val roadmapPort: RoadmapPort,
    private val progressPort: RoadmapProgressPort,
) : GetUserRoadmapProgressUseCase {

    private val logger = LoggerFactory.getLogger(GetUserRoadmapProgressService::class.java)

    override fun getProgress(userId: Long, roadmapSlug: String): UserProgressResult {
        logger.debug("진행 상황 조회 시작 - userId: {}, roadmapSlug: {}", userId, roadmapSlug)

        // 1. 로드맵 조회
        val roadmap = findRoadmapBySlug(roadmapSlug)

        // 2. 진행 상황 조회 (없으면 빈 진행 상황 반환)
        val userIdVO = UserId.of(userId)
        val progress = progressPort.findByUserAndRoadmap(userIdVO, roadmap.id!!)

        // 3. 진행률 계산
        val totalNodes = roadmap.nodes.size
        val statistics = progress?.calculateProgress(totalNodes)

        logger.info(
            "진행 상황 조회 완료 - userId: {}, roadmapId: {}, progress: {}%",
            userId,
            roadmap.id,
            statistics?.progressPercent ?: 0.0,
        )

        return UserProgressResult(
            userId = userId,
            roadmapId = roadmap.id,
            roadmapName = roadmap.name,
            roadmapSlug = roadmap.slug,
            done = progress?.done ?: emptySet(),
            learning = progress?.learning ?: emptySet(),
            skipped = progress?.skipped ?: emptySet(),
            isFavorite = progress?.isFavorite ?: false,
            totalNodes = totalNodes,
            doneCount = statistics?.doneCount ?: 0,
            learningCount = statistics?.learningCount ?: 0,
            skippedCount = statistics?.skippedCount ?: 0,
            progressPercent = statistics?.progressPercent ?: 0.0,
        )
    }

    override fun getUserRoadmaps(userId: Long): List<UserRoadmapSummary> {
        logger.debug("사용자의 모든 로드맵 조회 시작 - userId: {}", userId)

        val userIdVO = UserId.of(userId)
        val progressList = progressPort.findByUser(userIdVO)

        val summaries = progressList.mapNotNull { progress ->
            toUserRoadmapSummary(progress)
        }

        logger.info("사용자의 로드맵 조회 완료 - userId: {}, count: {}", userId, summaries.size)
        return summaries
    }

    override fun getFavoriteRoadmaps(userId: Long): List<UserRoadmapSummary> {
        logger.debug("즐겨찾기 로드맵 조회 시작 - userId: {}", userId)

        val userIdVO = UserId.of(userId)
        val progressList = progressPort.findFavoritesByUser(userIdVO)

        val summaries = progressList.mapNotNull { progress ->
            toUserRoadmapSummary(progress)
        }

        logger.info("즐겨찾기 로드맵 조회 완료 - userId: {}, count: {}", userId, summaries.size)
        return summaries
    }

    /**
     * 슬러그로 로드맵 조회
     */
    private fun findRoadmapBySlug(slug: String): Roadmap {
        val roadmapSlug = RoadmapSlug.of(slug)
        return roadmapPort.findBySlug(roadmapSlug)
            ?: throw BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                mapOf("slug" to slug, "message" to "로드맵을 찾을 수 없습니다"),
            )
    }

    /**
     * 진행 상황을 사용자 로드맵 요약으로 변환
     */
    private fun toUserRoadmapSummary(progress: UserRoadmapProgress): UserRoadmapSummary? {
        // 로드맵 조회
        val roadmap = roadmapPort.findById(progress.skillId) ?: run {
            logger.warn("로드맵을 찾을 수 없습니다 - skillId: {}", progress.skillId)
            return null
        }

        // 진행률 계산
        val totalNodes = roadmap.nodes.size
        val statistics = progress.calculateProgress(totalNodes)

        return UserRoadmapSummary(
            roadmapId = roadmap.id!!,
            roadmapName = roadmap.name,
            roadmapSlug = roadmap.slug,
            progressPercent = statistics.progressPercent,
            doneCount = statistics.doneCount,
            totalNodes = totalNodes,
            isFavorite = progress.isFavorite,
            lastUpdated = progress.updatedAt?.format(DateTimeFormatter.ISO_DATE_TIME) ?: "",
        )
    }
}
