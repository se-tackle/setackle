package org.setackle.backend.application.user.outbound

import org.setackle.backend.domain.user.model.UserRoadmapProgress
import org.setackle.backend.domain.user.vo.UserId

/**
 * 사용자 로드맵 진행도 데이터 접근 포트 (출력 포트)
 *
 * Infrastructure 레이어에서 구현되며, 사용자의 로드맵 진행 상황 영속성을 담당합니다.
 */
interface RoadmapProgressPort {
    /**
     * 사용자와 로드맵으로 진행 상황 조회
     *
     * @param userId 사용자 ID
     * @param skillId 스킬(로드맵) ID
     * @return 진행 상황 (없으면 null)
     */
    fun findByUserAndRoadmap(userId: UserId, skillId: Long): UserRoadmapProgress?

    /**
     * 진행 상황 저장
     *
     * @param progress 저장할 진행 상황
     * @return 저장된 진행 상황
     */
    fun save(progress: UserRoadmapProgress): UserRoadmapProgress

    /**
     * 진행 상황 삭제
     *
     * @param progress 삭제할 진행 상황
     */
    fun delete(progress: UserRoadmapProgress)

    /**
     * 사용자의 모든 진행 상황 조회
     *
     * @param userId 사용자 ID
     * @return 진행 상황 목록
     */
    fun findByUser(userId: UserId): List<UserRoadmapProgress>

    /**
     * 사용자의 즐겨찾기 로드맵 목록 조회
     *
     * @param userId 사용자 ID
     * @return 즐겨찾기된 진행 상황 목록
     */
    fun findFavoritesByUser(userId: UserId): List<UserRoadmapProgress>

    /**
     * 진행 상황 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @param skillId 스킬(로드맵) ID
     * @return 존재 여부
     */
    fun existsByUserAndRoadmap(userId: UserId, skillId: Long): Boolean
}
