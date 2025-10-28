package org.setackle.backend.infrastructure.persistence.user.repository

import org.setackle.backend.infrastructure.persistence.user.entity.UserRoadmapProgressJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRoadmapProgressJpaRepository : JpaRepository<UserRoadmapProgressJpaEntity, Long> {

    // 사용자 + 스킬 조합으로 진행 상황 조회
    fun findByUserIdAndSkillId(userId: Long, skillId: Long): UserRoadmapProgressJpaEntity?

    fun existsByUserIdAndSkillId(userId: Long, skillId: Long): Boolean

    // 사용자별 모든 진행 상황 조회
    fun findAllByUserId(userId: Long): List<UserRoadmapProgressJpaEntity>

    // 사용자별 즐겨찾기 로드맵 조회
    @Query(
        "SELECT p FROM UserRoadmapProgressJpaEntity p " +
            "WHERE p.userId = :userId AND p.isFavorite = true " +
            "ORDER BY p.updatedAt DESC",
    )
    fun findAllFavoritesByUserId(userId: Long): List<UserRoadmapProgressJpaEntity>

    // 스킬별 진행 상황 조회 (통계용)
    fun findAllBySkillId(skillId: Long): List<UserRoadmapProgressJpaEntity>

    // 사용자 + 스킬 조합 삭제
    fun deleteByUserIdAndSkillId(userId: Long, skillId: Long)

    // 사용자의 모든 진행 상황 삭제
    fun deleteAllByUserId(userId: Long)
}
