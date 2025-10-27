package org.setackle.backend.application.user.service

import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.setackle.backend.application.user.inbound.ResetProgressResult
import org.setackle.backend.application.user.inbound.ToggleFavoriteResult
import org.setackle.backend.application.user.inbound.UpdateProgressCommand
import org.setackle.backend.application.user.inbound.UpdateProgressResult
import org.setackle.backend.application.user.inbound.UpdateRoadmapProgressUseCase
import org.setackle.backend.application.user.outbound.ProgressEventPort
import org.setackle.backend.application.user.outbound.RoadmapProgressPort
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.domain.skill.model.Roadmap
import org.setackle.backend.domain.skill.vo.RoadmapSlug
import org.setackle.backend.domain.user.model.ProgressStatus
import org.setackle.backend.domain.user.model.UserRoadmapProgress
import org.setackle.backend.domain.user.vo.UserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 로드맵 진행도 업데이트 서비스
 *
 * 사용자의 로드맵 학습 진행 상태를 관리합니다.
 */
@Service
@Transactional
class UpdateRoadmapProgressService(
    private val roadmapPort: RoadmapPort,
    private val progressPort: RoadmapProgressPort,
    private val eventPort: ProgressEventPort,
) : UpdateRoadmapProgressUseCase {

    private val logger = LoggerFactory.getLogger(UpdateRoadmapProgressService::class.java)

    override fun updateProgress(command: UpdateProgressCommand): UpdateProgressResult {
        logger.debug(
            "진행도 업데이트 시작 - userId: {}, roadmapSlug: {}, nodeId: {}, status: {}",
            command.userId,
            command.roadmapSlug,
            command.nodeId,
            command.status,
        )

        // 1. 로드맵 조회
        val roadmap = findRoadmapBySlug(command.roadmapSlug)

        // 2. 노드 존재 여부 확인
        validateNodeExists(roadmap, command.nodeId)

        // 3. 진행 상황 조회 또는 생성
        val userId = UserId.of(command.userId)
        val progress = progressPort.findByUserAndRoadmap(userId, roadmap.id!!)
            ?: createNewProgress(userId, roadmap.id)

        // 4. 진행 상태 업데이트
        val status = parseProgressStatus(command.status)
        val updateResult = progress.updateNodeProgress(command.nodeId, status)

        // 5. 진행 상황 저장
        val savedProgress = progressPort.save(progress)

        // 6. 진행률 계산
        val totalNodes = roadmap.nodes.size
        val statistics = savedProgress.calculateProgress(totalNodes)

        // 7. 이벤트 발행 (선택적 - 추후 구현)
        // eventPort.publish(ProgressUpdatedEvent(...))

        logger.info(
            "진행도 업데이트 완료 - userId: {}, roadmapId: {}, nodeId: {}, {} -> {}",
            command.userId,
            roadmap.id,
            command.nodeId,
            updateResult.previousStatus,
            updateResult.currentStatus,
        )

        return UpdateProgressResult(
            userId = command.userId,
            roadmapId = roadmap.id,
            nodeId = command.nodeId,
            previousStatus = updateResult.previousStatus.name,
            currentStatus = updateResult.currentStatus.name,
            done = savedProgress.done,
            learning = savedProgress.learning,
            skipped = savedProgress.skipped,
            progressPercent = statistics.progressPercent,
        )
    }

    override fun resetProgress(userId: Long, roadmapSlug: String): ResetProgressResult {
        logger.debug("진행도 초기화 시작 - userId: {}, roadmapSlug: {}", userId, roadmapSlug)

        // 1. 로드맵 조회
        val roadmap = findRoadmapBySlug(roadmapSlug)

        // 2. 진행 상황 조회
        val userIdVO = UserId.of(userId)
        val progress = progressPort.findByUserAndRoadmap(userIdVO, roadmap.id!!)
            ?: throw BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                mapOf("message" to "진행 상황을 찾을 수 없습니다"),
            )

        // 3. 진행 상황 초기화
        progress.resetProgress()

        // 4. 저장
        progressPort.save(progress)

        logger.info("진행도 초기화 완료 - userId: {}, roadmapId: {}", userId, roadmap.id)

        return ResetProgressResult(
            userId = userId,
            roadmapId = roadmap.id,
            message = "진행 상황이 초기화되었습니다",
        )
    }

    override fun toggleFavorite(userId: Long, roadmapSlug: String): ToggleFavoriteResult {
        logger.debug("즐겨찾기 토글 시작 - userId: {}, roadmapSlug: {}", userId, roadmapSlug)

        // 1. 로드맵 조회
        val roadmap = findRoadmapBySlug(roadmapSlug)

        // 2. 진행 상황 조회 또는 생성
        val userIdVO = UserId.of(userId)
        val progress = progressPort.findByUserAndRoadmap(userIdVO, roadmap.id!!)
            ?: createNewProgress(userIdVO, roadmap.id)

        // 3. 즐겨찾기 토글
        progress.isFavorite = !progress.isFavorite

        // 4. 저장
        val savedProgress = progressPort.save(progress)

        logger.info(
            "즐겨찾기 토글 완료 - userId: {}, roadmapId: {}, isFavorite: {}",
            userId,
            roadmap.id,
            savedProgress.isFavorite,
        )

        return ToggleFavoriteResult(
            userId = userId,
            roadmapId = roadmap.id,
            isFavorite = savedProgress.isFavorite,
        )
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
     * 노드 존재 여부 확인
     */
    private fun validateNodeExists(roadmap: Roadmap, nodeId: String) {
        roadmap.findNode(nodeId)
            ?: throw BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                mapOf("nodeId" to nodeId, "message" to "노드를 찾을 수 없습니다"),
            )
    }

    /**
     * 새로운 진행 상황 생성
     */
    private fun createNewProgress(userId: UserId, skillId: Long): UserRoadmapProgress {
        logger.debug("새로운 진행 상황 생성 - userId: {}, skillId: {}", userId.value, skillId)
        return UserRoadmapProgress.create(userId, skillId)
    }

    /**
     * 문자열을 ProgressStatus enum으로 변환
     */
    private fun parseProgressStatus(status: String): ProgressStatus {
        return try {
            ProgressStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BusinessException(
                ErrorCode.INVALID_INPUT_VALUE,
                mapOf("status" to status, "message" to "유효하지 않은 진행 상태입니다"),
            )
        }
    }
}
