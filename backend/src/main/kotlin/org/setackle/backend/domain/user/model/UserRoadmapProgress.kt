package org.setackle.backend.domain.user.model

import org.setackle.backend.domain.user.vo.UserId
import java.time.LocalDateTime

/**
 * 사용자 로드맵 진행 상황
 *
 * UserRoadmapProgress 엔티티
 * - 사용자별 로드맵 진행 상황 관리
 * - done, learning, skipped, pending 상태 추적
 * - 진행률 계산 및 통계 제공
 *
 * @property id 진행 상황 고유 ID
 * @property userId 사용자 ID
 * @property skillId 스킬(로드맵) ID
 * @property done 완료한 노드 ID 집합
 * @property learning 학습 중인 노드 ID 집합
 * @property skipped 건너뛴 노드 ID 집합
 * @property isFavorite 즐겨찾기 여부
 * @property createdAt 생성 일시
 * @property updatedAt 수정 일시
 */
class UserRoadmapProgress(
    val id: Long?,
    val userId: UserId,
    val skillId: Long,
    private val _done: MutableSet<String> = mutableSetOf(),
    private val _learning: MutableSet<String> = mutableSetOf(),
    private val _skipped: MutableSet<String> = mutableSetOf(),
    var isFavorite: Boolean = false,
    val createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
) {
    /**
     * 읽기 전용 완료 노드 목록
     */
    val done: Set<String> get() = _done.toSet()

    /**
     * 읽기 전용 학습 중 노드 목록
     */
    val learning: Set<String> get() = _learning.toSet()

    /**
     * 읽기 전용 건너뛴 노드 목록
     */
    val skipped: Set<String> get() = _skipped.toSet()

    /**
     * 완료한 노드 개수
     */
    val doneCount: Int get() = _done.size

    /**
     * 학습 중인 노드 개수
     */
    val learningCount: Int get() = _learning.size

    /**
     * 건너뛴 노드 개수
     */
    val skippedCount: Int get() = _skipped.size

    /**
     * 노드 진행 상태 업데이트
     *
     * @param nodeId 업데이트할 노드 ID
     * @param status 새로운 진행 상태
     * @return 업데이트 결과 정보
     */
    fun updateNodeProgress(nodeId: String, status: ProgressStatus): ProgressUpdateResult {
        require(nodeId.isNotBlank()) { "노드 ID는 비어있을 수 없습니다" }

        val previousStatus = getNodeStatus(nodeId)

        // 이전 상태에서 제거
        _done.remove(nodeId)
        _learning.remove(nodeId)
        _skipped.remove(nodeId)

        // 새로운 상태로 설정
        when (status) {
            ProgressStatus.DONE -> _done.add(nodeId)
            ProgressStatus.LEARNING -> _learning.add(nodeId)
            ProgressStatus.SKIPPED -> _skipped.add(nodeId)
            ProgressStatus.PENDING -> {
                /* 모든 상태에서 제거됨 */
            }
        }

        updatedAt = LocalDateTime.now()

        return ProgressUpdateResult(
            nodeId = nodeId,
            previousStatus = previousStatus,
            currentStatus = status,
            updatedAt = updatedAt!!,
        )
    }

    /**
     * 여러 노드의 진행 상태 일괄 업데이트
     *
     * @param updates 노드 ID와 상태 맵
     * @return 업데이트 결과 목록
     */
    fun bulkUpdateProgress(updates: Map<String, ProgressStatus>): List<ProgressUpdateResult> {
        return updates.map { (nodeId, status) ->
            updateNodeProgress(nodeId, status)
        }
    }

    /**
     * 특정 노드의 현재 상태 확인
     *
     * @param nodeId 확인할 노드 ID
     * @return 현재 진행 상태
     */
    fun getNodeStatus(nodeId: String): ProgressStatus {
        return when {
            _done.contains(nodeId) -> ProgressStatus.DONE
            _learning.contains(nodeId) -> ProgressStatus.LEARNING
            _skipped.contains(nodeId) -> ProgressStatus.SKIPPED
            else -> ProgressStatus.PENDING
        }
    }

    /**
     * 노드가 완료 상태인지 확인
     */
    fun isDone(nodeId: String): Boolean = _done.contains(nodeId)

    /**
     * 노드가 학습 중 상태인지 확인
     */
    fun isLearning(nodeId: String): Boolean = _learning.contains(nodeId)

    /**
     * 노드가 건너뛴 상태인지 확인
     */
    fun isSkipped(nodeId: String): Boolean = _skipped.contains(nodeId)

    /**
     * 진행률 계산
     *
     * @param totalNodes 전체 노드 개수
     * @return 진행 통계 정보
     */
    fun calculateProgress(totalNodes: Int): ProgressStatistics {
        require(totalNodes >= 0) { "전체 노드 개수는 0 이상이어야 합니다" }

        return ProgressStatistics(
            totalNodes = totalNodes,
            doneCount = doneCount,
            learningCount = learningCount,
            skippedCount = skippedCount,
            progressPercent = if (totalNodes > 0) {
                (doneCount.toDouble() / totalNodes) * 100
            } else {
                0.0
            },
        )
    }

    /**
     * 모든 진행 상황 초기화
     */
    fun resetProgress() {
        _done.clear()
        _learning.clear()
        _skipped.clear()
        updatedAt = LocalDateTime.now()
    }

    /**
     * 즐겨찾기 토글
     *
     * @return 변경된 즐겨찾기 상태
     */
    fun toggleFavorite(): Boolean {
        isFavorite = !isFavorite
        updatedAt = LocalDateTime.now()
        return isFavorite
    }

    /**
     * 특정 상태의 노드 ID 목록 가져오기
     */
    fun getNodesByStatus(status: ProgressStatus): Set<String> {
        return when (status) {
            ProgressStatus.DONE -> done
            ProgressStatus.LEARNING -> learning
            ProgressStatus.SKIPPED -> skipped
            ProgressStatus.PENDING -> emptySet()
        }
    }

    companion object {
        /**
         * 새로운 진행 상황 생성 (팩토리 메서드)
         *
         * @param userId 사용자 ID
         * @param skillId 스킬 ID
         * @return 새로 생성된 UserRoadmapProgress 인스턴스
         */
        fun create(userId: UserId, skillId: Long): UserRoadmapProgress {
            require(skillId > 0) { "스킬 ID는 양수여야 합니다" }

            return UserRoadmapProgress(
                id = null,
                userId = userId,
                skillId = skillId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        }

        /**
         * DB에서 재구성 (팩토리 메서드)
         *
         * @return 재구성된 UserRoadmapProgress 인스턴스
         */
        fun reconstruct(
            id: Long,
            userId: UserId,
            skillId: Long,
            done: Set<String>,
            learning: Set<String>,
            skipped: Set<String>,
            isFavorite: Boolean,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
        ): UserRoadmapProgress {
            return UserRoadmapProgress(
                id = id,
                userId = userId,
                skillId = skillId,
                _done = done.toMutableSet(),
                _learning = learning.toMutableSet(),
                _skipped = skipped.toMutableSet(),
                isFavorite = isFavorite,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserRoadmapProgress

        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "UserRoadmapProgress(" +
            "id=$id, " +
            "userId=${userId.value}, " +
            "skillId=$skillId, " +
            "done=$doneCount, " +
            "learning=$learningCount, " +
            "skipped=$skippedCount" +
            ")"
}

/**
 * 진행 상태
 *
 * @property PENDING 시작 전 (기본 상태)
 * @property DONE 완료
 * @property LEARNING 학습 중
 * @property SKIPPED 건너뜀
 */
enum class ProgressStatus {
    PENDING,
    DONE,
    LEARNING,
    SKIPPED,
}

/**
 * 진행 상황 업데이트 결과
 *
 * @property nodeId 업데이트된 노드 ID
 * @property previousStatus 이전 상태
 * @property currentStatus 현재 상태
 * @property updatedAt 업데이트 일시
 */
data class ProgressUpdateResult(
    val nodeId: String,
    val previousStatus: ProgressStatus,
    val currentStatus: ProgressStatus,
    val updatedAt: LocalDateTime,
) {
    /**
     * 상태 변경 여부
     */
    fun isChanged(): Boolean = previousStatus != currentStatus

    /**
     * 완료 상태로 변경되었는지 확인
     */
    fun isCompletedNow(): Boolean = currentStatus == ProgressStatus.DONE && previousStatus != ProgressStatus.DONE
}

/**
 * 진행 통계
 *
 * @property totalNodes 전체 노드 개수
 * @property doneCount 완료한 노드 개수
 * @property learningCount 학습 중인 노드 개수
 * @property skippedCount 건너뛴 노드 개수
 * @property progressPercent 진행률 (0.0 ~ 100.0)
 */
data class ProgressStatistics(
    val totalNodes: Int,
    val doneCount: Int,
    val learningCount: Int,
    val skippedCount: Int,
    val progressPercent: Double,
) {
    /**
     * 대기 중인 노드 개수
     */
    val pendingCount: Int = totalNodes - doneCount - learningCount - skippedCount

    /**
     * 진행 중인 노드 개수 (완료 + 학습 중)
     */
    val activeCount: Int = doneCount + learningCount

    /**
     * 완료율이 100%인지 확인
     */
    fun isCompleted(): Boolean = doneCount == totalNodes && totalNodes > 0

    /**
     * 시작했는지 확인 (하나 이상의 노드가 진행 중)
     */
    fun isStarted(): Boolean = activeCount > 0
}
