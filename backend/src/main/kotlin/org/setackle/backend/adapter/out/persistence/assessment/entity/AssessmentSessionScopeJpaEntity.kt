package org.setackle.backend.adapter.out.persistence.assessment.entity

import jakarta.persistence.*
import org.setackle.backend.domain.model.assessment.AssessmentSessionScope

@Entity
@Table(name = "assessment_session_scope")
@IdClass(AssessmentSessionScopeId::class)
class AssessmentSessionScopeJpaEntity(
    @Id
    @Column(name = "session_id")
    val sessionId: Long,

    @Id
    @Column(name = "node_id")
    val nodeId: Long
) {
    fun toDomain(): AssessmentSessionScope {
        return AssessmentSessionScope(
            sessionId = sessionId,
            nodeId = nodeId
        )
    }

    companion object {
        fun fromDomain(assessmentSessionScope: AssessmentSessionScope): AssessmentSessionScopeJpaEntity {
            return AssessmentSessionScopeJpaEntity(
                sessionId = assessmentSessionScope.sessionId,
                nodeId = assessmentSessionScope.nodeId
            )
        }
    }
}

@Embeddable
data class AssessmentSessionScopeId(
    val sessionId: Long = 0,
    val nodeId: Long = 0
) : java.io.Serializable