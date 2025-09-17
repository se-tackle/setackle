package org.setackle.backend.adapter.out.persistence.question.entity

import jakarta.persistence.*
import org.setackle.backend.domain.model.question.QuestionOption
import java.time.LocalDateTime

@Entity
@Table(name = "question_options")
class QuestionOptionJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "question_id", nullable = false)
    val questionId: Long,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(name = "is_correct", nullable = false)
    var isCorrect: Boolean = false,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): QuestionOption {
        return QuestionOption(
            id = id,
            questionId = questionId,
            content = content,
            isCorrect = isCorrect,
            orderIndex = orderIndex,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(questionOption: QuestionOption): QuestionOptionJpaEntity {
            return QuestionOptionJpaEntity(
                id = questionOption.id,
                questionId = questionOption.questionId,
                content = questionOption.content,
                isCorrect = questionOption.isCorrect,
                orderIndex = questionOption.orderIndex,
                createdAt = questionOption.createdAt ?: LocalDateTime.now(),
                updatedAt = questionOption.updatedAt ?: LocalDateTime.now()
            )
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}