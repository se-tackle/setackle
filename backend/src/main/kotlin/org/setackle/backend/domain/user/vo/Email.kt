package org.setackle.backend.domain.user.vo

import java.util.regex.Pattern

/**
 * 이메일 값 객체
 * 이메일의 형식 검증과 정규화를 담당하는 불변 값 객체
 */
class Email private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "이메일은 빈 값일 수 없습니다." }
        require(isValidFormat(value)) { "유효하지 않은 이메일 형식입니다." }
        require(value.length <= 254) { "이메일은 254자를 초과할 수 없습니다." }
        require(!isBlockedDomain(value)) { "임시 이메일 주소는 사용할 수 없습니다." }
        require(isValidLocalPart(value)) { "이메일 형식이 올바르지 않습니다." }
    }

    /**
     * 이메일 정규화 (소문자 변환, 공백 제거 적용된 값)
     */
    val normalized: String = value.trim().lowercase()

    /**
     * 도메인 부분 추출
     */
    val domain: String = normalized.substringAfter("@")

    /**
     * 로컬 부분 추출
     */
    val localPart: String = normalized.substringBefore("@")

    /**
     * 개인정보 보호를 위한 마스킹된 이메일
     */
    val masked: String by lazy {
        when {
            localPart.length <= 2 -> "*".repeat(localPart.length) + "@$domain"
            localPart.length <= 4 -> localPart.first() + "*".repeat(localPart.length - 2) + localPart.last() + "@$domain"
            else -> localPart.take(2) + "*".repeat(localPart.length - 4) + localPart.takeLast(2) + "@$domain"
        }
    }

    override fun toString(): String = masked

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Email) return false
        return normalized == other.normalized
    }

    override fun hashCode(): Int = normalized.hashCode()

    /**
     * 안전한 복사 메소드 (팩토리 메소드를 통한 검증)
     */
    fun copy(newValue: String = this.value): Email = of(newValue)

    companion object {
        // RFC 5322 기반 이메일 정규식 (단순화된 버전)
        private val EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        )

        // 금지된 이메일 도메인 (예시)
        private val BLOCKED_DOMAINS = setOf(
            "tempmail.com",
            "10minutemail.com",
            "guerrillamail.com",
            "mailinator.com",
            "throwaway.email",
        )

        /**
         * 이메일 생성 팩토리 메소드
         * 검증 실패 시 예외 발생
         */
        fun of(email: String): Email {
            val normalized = email.trim().lowercase()
            return Email(normalized)
        }

        /**
         * 안전한 이메일 생성 메소드
         * 검증 실패 시 null 반환
         */
        fun ofOrNull(email: String?): Email? {
            return try {
                email?.let { of(it) }
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        private fun isValidFormat(email: String): Boolean {
            return EMAIL_PATTERN.matcher(email.lowercase()).matches()
        }

        private fun isBlockedDomain(email: String): Boolean {
            val domain = email.lowercase().substringAfter("@")
            return domain in BLOCKED_DOMAINS
        }

        private fun isValidLocalPart(email: String): Boolean {
            val localPart = email.substringBefore("@")

            // RFC 5321 제한 (64자)
            if (localPart.length > 64) return false

            // 연속된 점 검증
            if (localPart.contains("..")) return false

            // 시작/끝 점 검증
            if (localPart.startsWith(".") || localPart.endsWith(".")) return false

            return true
        }
    }
}
