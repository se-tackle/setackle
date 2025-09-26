package org.setackle.backend.domain.user.model

/**
 * 사용자명 값 객체
 * 사용자명의 유효성 검증과 정규화를 담당하는 불변 값 객체
 */
class Username private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "사용자명은 빈 값일 수 없습니다." }
        require(isValidLength(value)) { "사용자명은 2자 이상 30자 이하여야 합니다." }
        require(isValidFormat(value)) { "사용자명은 영문, 숫자, 밑줄(_), 하이픈(-)만 사용할 수 있습니다." }
        require(!isNumericOnly(value)) { "사용자명은 숫자로만 구성될 수 없습니다." }
        require(!isReservedName(value)) { "사용할 수 없는 사용자명입니다." }
    }

    /**
     * 정규화된 사용자명 (공백 제거 적용)
     */
    val normalized: String = value.trim()

    override fun toString(): String = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Username) return false
        return normalized == other.normalized
    }

    override fun hashCode(): Int = normalized.hashCode()

    /**
     * 안전한 복사 메소드 (팩토리 메소드를 통한 검증)
     */
    fun copy(newValue: String = this.value): Username = of(newValue)

    companion object {
        private const val MIN_LENGTH = 2
        private const val MAX_LENGTH = 30

        // 허용되는 문자 패턴 (영문, 숫자, 밑줄, 하이픈)
        private val ALLOWED_PATTERN = Regex("^[a-zA-Z0-9_-]+$")

        // 예약어 목록
        private val RESERVED_NAMES = setOf(
            "admin", "administrator", "root", "user", "test", "guest", "system",
            "api", "www", "mail", "ftp", "support", "help", "info", "service",
            "null", "undefined", "void", "setackle", "app", "application"
        )

        /**
         * 사용자명 생성 팩토리 메소드
         * 검증 실패 시 예외 발생
         */
        fun of(username: String): Username {
            val trimmed = username.trim()
            return Username(trimmed)
        }

        /**
         * 안전한 사용자명 생성 메소드
         * 검증 실패 시 null 반환
         */
        fun ofOrNull(username: String?): Username? {
            return try {
                username?.let { of(it) }
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        /**
         * 사용자명 가용성 확인 (형식만 검증)
         */
        fun isAvailable(username: String): Boolean {
            return try {
                of(username)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        private fun isValidLength(username: String): Boolean {
            val trimmed = username.trim()
            return trimmed.length in MIN_LENGTH..MAX_LENGTH
        }

        private fun isValidFormat(username: String): Boolean {
            return ALLOWED_PATTERN.matches(username.trim())
        }

        private fun isNumericOnly(username: String): Boolean {
            return username.trim().all { it.isDigit() }
        }

        private fun isReservedName(username: String): Boolean {
            return username.trim().lowercase() in RESERVED_NAMES
        }
    }
}