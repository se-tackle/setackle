package org.setackle.backend.domain.user.vo

enum class UserRole(val displayName: String) {
    USER("일반 사용자"),
    ADMIN("관리자"),
    ;

    fun hasPermission(requiredRole: UserRole): Boolean {
        return when (this) {
            ADMIN -> true
            USER -> requiredRole == USER
        }
    }
}