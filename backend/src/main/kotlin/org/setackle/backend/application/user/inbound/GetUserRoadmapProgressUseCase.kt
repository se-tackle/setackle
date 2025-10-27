package org.setackle.backend.application.user.inbound

/**
 * 사용자 로드맵 진행도 조회 UseCase
 *
 * 사용자의 로드맵 학습 진행 상황을 조회합니다.
 */
interface GetUserRoadmapProgressUseCase {
    /**
     * 특정 로드맵의 사용자 진행 상황 조회
     *
     * @param userId 사용자 ID
     * @param roadmapSlug 로드맵 슬러그
     * @return 사용자 진행 상황
     * @throws BusinessException 로드맵을 찾을 수 없는 경우
     */
    fun getProgress(userId: Long, roadmapSlug: String): UserProgressResult

    /**
     * 사용자의 모든 진행 중인 로드맵 목록 조회
     *
     * @param userId 사용자 ID
     * @return 진행 중인 로드맵 목록
     */
    fun getUserRoadmaps(userId: Long): List<UserRoadmapSummary>

    /**
     * 사용자의 즐겨찾기 로드맵 목록 조회
     *
     * @param userId 사용자 ID
     * @return 즐겨찾기 로드맵 목록
     */
    fun getFavoriteRoadmaps(userId: Long): List<UserRoadmapSummary>
}

/**
 * 사용자 진행 상황 결과
 */
data class UserProgressResult(
    val userId: Long,
    val roadmapId: Long,
    val roadmapName: String,
    val roadmapSlug: String,
    val done: Set<String>,
    val learning: Set<String>,
    val skipped: Set<String>,
    val isFavorite: Boolean,
    val totalNodes: Int,
    val doneCount: Int,
    val learningCount: Int,
    val skippedCount: Int,
    val progressPercent: Double,
)

/**
 * 사용자 로드맵 요약
 */
data class UserRoadmapSummary(
    val roadmapId: Long,
    val roadmapName: String,
    val roadmapSlug: String,
    val progressPercent: Double,
    val doneCount: Int,
    val totalNodes: Int,
    val isFavorite: Boolean,
    val lastUpdated: String, // ISO-8601 형식
)
