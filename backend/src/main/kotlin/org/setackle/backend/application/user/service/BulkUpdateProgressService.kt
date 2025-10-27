package org.setackle.backend.application.user.service

import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.setackle.backend.application.user.inbound.BulkUpdateError
import org.setackle.backend.application.user.inbound.BulkUpdateProgressCommand
import org.setackle.backend.application.user.inbound.BulkUpdateProgressResult
import org.setackle.backend.application.user.inbound.BulkUpdateProgressUseCase
import org.setackle.backend.application.user.inbound.NodeStatusUpdate
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
 * 일괄 진행도 업데이트 서비스
 *
 * 여러 노드의 진행 상태를 동시에 업데이트합니다.
 * 부분 실패 허용: 일부 노드 업데이트 실패 시에도 나머지 노드는 처리됩니다.
 */
@Service
@Transactional
class BulkUpdateProgressService(
    private val roadmapPort: RoadmapPort,
    private val progressPort: RoadmapProgressPort,
    private val eventPort: ProgressEventPort,
) : BulkUpdateProgressUseCase {

    private val logger = LoggerFactory.getLogger(BulkUpdateProgressService::class.java)

    override fun bulkUpdate(command: BulkUpdateProgressCommand): BulkUpdateProgressResult {
        logger.debug(
            "일괄 업데이트 시작 - userId: {}, roadmapSlug: {}, updateCount: {}",
            command.userId,
            command.roadmapSlug,
            command.updates.size,
        )

        // 1. 입력 검증
        validateCommand(command)

        // 2. 로드맵 조회
        val roadmap = findRoadmapBySlug(command.roadmapSlug)

        // 3. 진행 상황 조회 또는 생성
        val userId = UserId.of(command.userId)
        val progress = progressPort.findByUserAndRoadmap(userId, roadmap.id!!)
            ?: createNewProgress(userId, roadmap.id)

        // 4. 일괄 업데이트 처리
        val (successCount, errors) = processBulkUpdate(progress, roadmap, command.updates)

        // 5. 진행 상황 저장
        val savedProgress = progressPort.save(progress)

        // 6. 진행률 계산
        val totalNodes = roadmap.nodes.size
        val statistics = savedProgress.calculateProgress(totalNodes)

        // 7. 이벤트 발행 (선택적 - 추후 구현)
        // eventPort.publish(ProgressBulkUpdatedEvent(...))

        logger.info(
            "일괄 업데이트 완료 - userId: {}, roadmapId: {}, 성공: {}/{}, 실패: {}",
            command.userId,
            roadmap.id,
            successCount,
            command.updates.size,
            errors.size,
        )

        return BulkUpdateProgressResult(
            userId = command.userId,
            roadmapId = roadmap.id,
            totalUpdated = command.updates.size,
            successCount = successCount,
            failedCount = errors.size,
            done = savedProgress.done,
            learning = savedProgress.learning,
            skipped = savedProgress.skipped,
            progressPercent = statistics.progressPercent,
            errors = errors.takeIf { it.isNotEmpty() },
        )
    }

    /**
     * 명령 검증
     */
    private fun validateCommand(command: BulkUpdateProgressCommand) {
        if (command.updates.isEmpty()) {
            throw BusinessException(
                ErrorCode.INVALID_INPUT_VALUE,
                mapOf("message" to "업데이트할 항목이 없습니다"),
            )
        }

        if (command.updates.size > MAX_BULK_UPDATE_SIZE) {
            throw BusinessException(
                ErrorCode.INVALID_INPUT_VALUE,
                mapOf(
                    "message" to "일괄 업데이트는 최대 ${MAX_BULK_UPDATE_SIZE}개까지 가능합니다",
                    "requested" to command.updates.size,
                ),
            )
        }
    }

    /**
     * 일괄 업데이트 처리
     *
     * 부분 실패를 허용하며, 성공한 업데이트 수와 에러 목록을 반환합니다.
     */
    private fun processBulkUpdate(
        progress: UserRoadmapProgress,
        roadmap: Roadmap,
        updates: List<NodeStatusUpdate>,
    ): Pair<Int, List<BulkUpdateError>> {
        var successCount = 0
        val errors = mutableListOf<BulkUpdateError>()

        // 각 업데이트를 개별적으로 처리
        updates.forEach { update ->
            try {
                // 노드 존재 여부 확인
                validateNodeExists(roadmap, update.nodeId)

                // 상태 파싱
                val status = parseProgressStatus(update.status)

                // 진행 상태 업데이트
                progress.updateNodeProgress(update.nodeId, status)

                successCount++
            } catch (e: BusinessException) {
                // 비즈니스 예외는 에러 목록에 추가하고 계속 진행
                logger.warn(
                    "노드 업데이트 실패 - nodeId: {}, reason: {}",
                    update.nodeId,
                    e.message,
                )
                errors.add(
                    BulkUpdateError(
                        nodeId = update.nodeId,
                        reason = e.message ?: "알 수 없는 오류",
                    ),
                )
            } catch (e: Exception) {
                // 예상치 못한 예외는 로그 후 에러 목록에 추가
                logger.error(
                    "노드 업데이트 중 예외 발생 - nodeId: {}",
                    update.nodeId,
                    e,
                )
                errors.add(
                    BulkUpdateError(
                        nodeId = update.nodeId,
                        reason = "시스템 오류가 발생했습니다",
                    ),
                )
            }
        }

        return Pair(successCount, errors)
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

    companion object {
        /**
         * 최대 일괄 업데이트 크기
         * 성능과 안정성을 고려하여 한 번에 처리할 수 있는 최대 노드 수 제한
         */
        private const val MAX_BULK_UPDATE_SIZE = 100
    }
}
