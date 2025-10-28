package org.setackle.backend.infrastructure.persistence.user.entity

import io.hypersistence.utils.hibernate.type.array.ListArrayType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.setackle.backend.domain.user.model.UserRoadmapProgress
import org.setackle.backend.domain.user.vo.UserId
import java.time.LocalDateTime

@Entity
@Table(name = "user_roadmap_progress")
class UserRoadmapProgressJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "skill_id", nullable = false)
    var skillId: Long,

    @Type(ListArrayType::class)
    @Column(name = "done", columnDefinition = "text[]")
    var done: List<String> = emptyList(),

    @Type(ListArrayType::class)
    @Column(name = "learning", columnDefinition = "text[]")
    var learning: List<String> = emptyList(),

    @Type(ListArrayType::class)
    @Column(name = "skipped", columnDefinition = "text[]")
    var skipped: List<String> = emptyList(),

    @Column(name = "is_favorite", nullable = false)
    var isFavorite: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    /**
     * UserRoadmapProgress 도메인 모델로 변환
     */
    fun toDomain(): UserRoadmapProgress {
        return UserRoadmapProgress.reconstruct(
            id = id!!,
            userId = UserId.of(userId),
            skillId = skillId,
            done = done.toSet(),
            learning = learning.toSet(),
            skipped = skipped.toSet(),
            isFavorite = isFavorite,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        /**
         * UserRoadmapProgress 도메인에서 JPA Entity로 변환
         */
        fun fromDomain(progress: UserRoadmapProgress): UserRoadmapProgressJpaEntity {
            return UserRoadmapProgressJpaEntity(
                id = progress.id,
                userId = progress.userId.value,
                skillId = progress.skillId,
                done = progress.done.toList(),
                learning = progress.learning.toList(),
                skipped = progress.skipped.toList(),
                isFavorite = progress.isFavorite,
                createdAt = progress.createdAt ?: LocalDateTime.now(),
                updatedAt = progress.updatedAt ?: LocalDateTime.now(),
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
