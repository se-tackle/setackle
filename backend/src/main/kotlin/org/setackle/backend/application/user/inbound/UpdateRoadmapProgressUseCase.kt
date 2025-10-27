package org.setackle.backend.application.user.inbound

/**
 * 로드맵 진행도 업데이트 UseCase
 *
 * 사용자의 로드맵 학습 진행 상태를 관리합니다.
 */
interface UpdateRoadmapProgressUseCase {
    /**
     * 노드 진행 상태 업데이트
     *
     * @param command 진행 상태 업데이트 명령
     * @return 업데이트 결과 (이전 상태, 현재 상태, 전체 진행률 포함)
     * @throws BusinessException 로드맵 또는 노드를 찾을 수 없는 경우
     */
    fun updateProgress(command: UpdateProgressCommand): UpdateProgressResult

    /**
     * 로드맵 진행 상황 초기화
     *
     * @param userId 사용자 ID
     * @param roadmapSlug 로드맵 슬러그
     * @return 초기화 결과
     * @throws BusinessException 로드맵을 찾을 수 없는 경우
     */
    fun resetProgress(userId: Long, roadmapSlug: String): ResetProgressResult

    /**
     * 로드맵 즐겨찾기 토글
     *
     * @param userId 사용자 ID
     * @param roadmapSlug 로드맵 슬러그
     * @return 즐겨찾기 토글 결과
     * @throws BusinessException 로드맵을 찾을 수 없는 경우
     */
    fun toggleFavorite(userId: Long, roadmapSlug: String): ToggleFavoriteResult
}

/**
 * 진행 상태 업데이트 명령
 */
data class UpdateProgressCommand(
    val userId: Long,
    val roadmapSlug: String,
    val nodeId: String,
    val status: String, // PENDING, DONE, LEARNING, SKIPPED
)

/**
 * 진행 상태 업데이트 결과
 */
data class UpdateProgressResult(
    val userId: Long,
    val roadmapId: Long,
    val nodeId: String,
    val previousStatus: String,
    val currentStatus: String,
    val done: Set<String>,
    val learning: Set<String>,
    val skipped: Set<String>,
    val progressPercent: Double,
)

/**
 * 진행 상황 초기화 결과
 */
data class ResetProgressResult(
    val userId: Long,
    val roadmapId: Long,
    val message: String,
)

/**
 * 즐겨찾기 토글 결과
 */
data class ToggleFavoriteResult(
    val userId: Long,
    val roadmapId: Long,
    val isFavorite: Boolean,
)
