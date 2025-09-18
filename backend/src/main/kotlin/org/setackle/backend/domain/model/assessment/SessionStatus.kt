package org.setackle.backend.domain.model.assessment

enum class SessionStatus(val displayName: String) {
    CREATED("생성됨"),
    IN_PROGRESS("진행중"),
    PAUSED("일시정지"),
    COMPLETED("완료됨"),
    EXPIRED("만료됨"),
    CANCELLED("취소됨"),
    ;

    fun canTransitionTo(newStatus: SessionStatus): Boolean {
        return when (this) {
            CREATED -> newStatus in setOf(IN_PROGRESS, CANCELLED)
            IN_PROGRESS -> newStatus in setOf(PAUSED, COMPLETED, EXPIRED, CANCELLED)
            PAUSED -> newStatus in setOf(IN_PROGRESS, COMPLETED, EXPIRED, CANCELLED)
            COMPLETED -> false
            EXPIRED -> false
            CANCELLED -> false
        }
    }

    fun isActive(): Boolean = this in setOf(CREATED, IN_PROGRESS, PAUSED)
    fun isFinished(): Boolean = this in setOf(COMPLETED, EXPIRED, CANCELLED)
}