package org.setackle.backend.domain.user.vo

/**
 * 비밀번호 강도 열거형
 */
enum class PasswordStrength(val level: Int, val description: String) {
    WEAK(1, "약함"),
    MEDIUM(2, "보통"),
    STRONG(3, "강함"),
    VERY_STRONG(4, "매우 강함")
}