package org.setackle.backend.application.user.inbound

/**
 * 일괄 진행도 업데이트 UseCase
 *
 * 여러 노드의 진행 상태를 동시에 업데이트합니다.
 * 평가 완료 후 자동 업데이트나 CSV 임포트 등에 활용됩니다.
 */
interface BulkUpdateProgressUseCase {
    /**
     * 여러 노드의 진행 상태를 일괄 업데이트
     *
     * @param command 일괄 업데이트 명령
     * @return 업데이트 결과
     * @throws BusinessException 로드맵을 찾을 수 없는 경우
     */
    fun bulkUpdate(command: BulkUpdateProgressCommand): BulkUpdateProgressResult
}

/**
 * 일괄 진행 상태 업데이트 명령
 */
data class BulkUpdateProgressCommand(
    val userId: Long,
    val roadmapSlug: String,
    val updates: List<NodeStatusUpdate>,
)

/**
 * 노드 상태 업데이트 항목
 */
data class NodeStatusUpdate(
    val nodeId: String,
    val status: String, // PENDING, DONE, LEARNING, SKIPPED
)

/**
 * 일괄 업데이트 결과
 */
data class BulkUpdateProgressResult(
    val userId: Long,
    val roadmapId: Long,
    val totalUpdated: Int,
    val successCount: Int,
    val failedCount: Int,
    val done: Set<String>,
    val learning: Set<String>,
    val skipped: Set<String>,
    val progressPercent: Double,
    val errors: List<BulkUpdateError>?,
)

/**
 * 일괄 업데이트 오류 항목
 */
data class BulkUpdateError(
    val nodeId: String,
    val reason: String,
)
