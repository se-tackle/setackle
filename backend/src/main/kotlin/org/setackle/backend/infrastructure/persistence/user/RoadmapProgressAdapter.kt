package org.setackle.backend.infrastructure.persistence.user

import org.setackle.backend.application.user.outbound.RoadmapProgressPort
import org.setackle.backend.domain.user.model.UserRoadmapProgress
import org.setackle.backend.domain.user.vo.UserId
import org.setackle.backend.infrastructure.persistence.user.entity.UserRoadmapProgressJpaEntity
import org.setackle.backend.infrastructure.persistence.user.repository.UserRoadmapProgressJpaRepository
import org.springframework.stereotype.Component

/**
 * 사용자 로드맵 진행도 관련 어댑터 (Port의 구현체)
 * UserRoadmapProgressJpaRepository를 사용하여 RoadmapProgressPort를 구현
 */
@Component
class RoadmapProgressAdapter(
    private val userRoadmapProgressJpaRepository: UserRoadmapProgressJpaRepository,
) : RoadmapProgressPort {

    override fun findByUserAndRoadmap(userId: UserId, skillId: Long): UserRoadmapProgress? {
        return userRoadmapProgressJpaRepository
            .findByUserIdAndSkillId(userId.value, skillId)
            ?.toDomain()
    }

    override fun save(progress: UserRoadmapProgress): UserRoadmapProgress {
        val entity = if (progress.id == null) {
            // 신규 생성
            UserRoadmapProgressJpaEntity.fromDomain(progress)
        } else {
            // 기존 엔티티 업데이트
            userRoadmapProgressJpaRepository.findById(progress.id)
                .map { existingEntity ->
                    existingEntity.apply {
                        done = progress.done.toList()
                        learning = progress.learning.toList()
                        skipped = progress.skipped.toList()
                        isFavorite = progress.isFavorite
                    }
                }
                .orElseThrow { IllegalArgumentException("Progress not found: ${progress.id}") }
        }

        val savedEntity = userRoadmapProgressJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun delete(progress: UserRoadmapProgress) {
        progress.id?.let { userRoadmapProgressJpaRepository.deleteById(it) }
            ?: throw IllegalArgumentException("Cannot delete progress without ID")
    }

    override fun findByUser(userId: UserId): List<UserRoadmapProgress> {
        return userRoadmapProgressJpaRepository
            .findAllByUserId(userId.value)
            .map { it.toDomain() }
    }

    override fun findFavoritesByUser(userId: UserId): List<UserRoadmapProgress> {
        return userRoadmapProgressJpaRepository
            .findAllFavoritesByUserId(userId.value)
            .map { it.toDomain() }
    }

    override fun existsByUserAndRoadmap(userId: UserId, skillId: Long): Boolean {
        return userRoadmapProgressJpaRepository
            .existsByUserIdAndSkillId(userId.value, skillId)
    }
}
