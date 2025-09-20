package org.setackle.backend.adapter.outbound.persistence.question.entity

import jakarta.persistence.*
import org.setackle.backend.domain.question.model.Question
import java.time.LocalDateTime

@Entity
@Table(name = "questions")
class QuestionJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "node_id", nullable = false)
    val nodeId: Long,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Question {
        return Question(
            id = id,
            nodeId = nodeId,
            content = content,
            description = description,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(question: Question): QuestionJpaEntity {
            return QuestionJpaEntity(
                id = question.id,
                nodeId = question.nodeId,
                content = question.content,
                description = question.description,
                isActive = question.isActive,
                createdAt = question.createdAt ?: LocalDateTime.now(),
                updatedAt = question.updatedAt ?: LocalDateTime.now()
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}