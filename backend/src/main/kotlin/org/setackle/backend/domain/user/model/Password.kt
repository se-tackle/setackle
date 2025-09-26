package org.setackle.backend.domain.user.model

/**
 * 비밀번호 값 객체
 * 비밀번호 정책 강제와 해싱을 담당하는 불변 값 객체
 */
class Password private constructor(val hashedValue: String) {

    /**
     * Raw 비밀번호로부터 생성하는 경우 (내부 생성용)
     */
    private constructor(rawPassword: String, encoder: (String) -> String) : this(encoder(rawPassword)) {
        validatePolicy(rawPassword)
    }

    override fun toString(): String = "[PROTECTED]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Password) return false
        return hashedValue == other.hashedValue
    }

    override fun hashCode(): Int = hashedValue.hashCode()

    /**
     * 비밀번호 매치 확인
     */
    fun matches(rawPassword: String, encoder: (String) -> String): Boolean {
        return encoder(rawPassword) == hashedValue
    }

    /**
     * 안전한 복사 메소드 (새로운 원시 비밀번호로 생성)
     */
    fun copy(newRawPassword: String, encoder: (String) -> String): Password = of(newRawPassword, encoder)

    companion object {
        private const val MIN_LENGTH = 8
        private const val MAX_LENGTH = 128

        /**
         * 원시 비밀번호로부터 Password 객체 생성 (해싱 포함)
         */
        fun of(rawPassword: String, encoder: (String) -> String): Password {
            validatePolicy(rawPassword)
            return Password(rawPassword, encoder)
        }

        /**
         * 이미 해싱된 비밀번호로부터 Password 객체 생성
         */
        fun fromHash(hashedPassword: String): Password {
            require(hashedPassword.isNotBlank()) { "해싱된 비밀번호는 빈 값일 수 없습니다." }
            return Password(hashedPassword)
        }

        /**
         * 안전한 Password 생성 메소드
         */
        fun ofOrNull(rawPassword: String?, encoder: (String) -> String): Password? {
            return try {
                rawPassword?.let { of(it, encoder) }
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        /**
         * 두 비밀번호가 동일한지 검증
         */
        fun matches(password: String, confirmPassword: String): Boolean {
            return password == confirmPassword
        }

        /**
         * 비밀번호 정책 검증
         */
        fun validatePolicy(password: String) {
            require(password.isNotBlank()) { "비밀번호는 빈 값일 수 없습니다." }

            // 길이 검증
            require(password.length >= MIN_LENGTH) {
                "비밀번호는 최소 ${MIN_LENGTH}자 이상이어야 합니다."
            }
            require(password.length <= MAX_LENGTH) {
                "비밀번호는 최대 ${MAX_LENGTH}자 이하여야 합니다."
            }

            // 대문자 포함 검증
            require(password.any { it.isUpperCase() }) {
                "비밀번호는 대문자를 최소 1개 포함해야 합니다."
            }

            // 소문자 포함 검증
            require(password.any { it.isLowerCase() }) {
                "비밀번호는 소문자를 최소 1개 포함해야 합니다."
            }

            // 숫자 포함 검증
            require(password.any { it.isDigit() }) {
                "비밀번호는 숫자를 최소 1개 포함해야 합니다."
            }

            // 특수문자 포함 검증
            val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"
            require(password.any { it in specialChars }) {
                "비밀번호는 특수문자(!@#$%^&*()_+-=[]{}|;:,.<>?)를 최소 1개 포함해야 합니다."
            }

            // 연속된 문자 검증 (3자 이상 연속 금지)
            require(!hasConsecutiveChars(password, 3)) {
                "비밀번호에 3자 이상 연속된 문자를 포함할 수 없습니다."
            }

            // 반복 문자 검증 (3자 이상 반복 금지)
            require(!hasRepeatingChars(password, 3)) {
                "비밀번호에 3자 이상 반복된 문자를 포함할 수 없습니다."
            }
        }

        /**
         * 비밀번호 강도 계산 (1-4점)
         */
        fun calculateStrength(password: String): PasswordStrength {
            var score = 0

            // 기본 길이 점수
            score += when {
                password.length >= 12 -> 2
                password.length >= 8 -> 1
                else -> 0
            }

            // 문자 종류 다양성 점수
            if (password.any { it.isLowerCase() }) score++
            if (password.any { it.isUpperCase() }) score++
            if (password.any { it.isDigit() }) score++
            if (password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) score++

            // 추가 보너스
            if (password.length >= 16) score++

            return when {
                score <= 2 -> PasswordStrength.WEAK
                score <= 4 -> PasswordStrength.MEDIUM
                score <= 6 -> PasswordStrength.STRONG
                else -> PasswordStrength.VERY_STRONG
            }
        }

        private fun hasConsecutiveChars(password: String, count: Int): Boolean {
            for (i in 0..password.length - count) {
                val substring = password.substring(i, i + count)
                var isConsecutive = true

                for (j in 1 until substring.length) {
                    if (substring[j].code != substring[j - 1].code + 1) {
                        isConsecutive = false
                        break
                    }
                }

                if (isConsecutive) return true
            }
            return false
        }

        private fun hasRepeatingChars(password: String, count: Int): Boolean {
            for (i in 0..password.length - count) {
                val char = password[i]
                var repeatingCount = 1

                for (j in i + 1 until password.length) {
                    if (password[j] == char) {
                        repeatingCount++
                        if (repeatingCount >= count) return true
                    } else {
                        break
                    }
                }
            }
            return false
        }
    }
}

/**
 * 비밀번호 강도 열거형
 */
enum class PasswordStrength(val level: Int, val description: String) {
    WEAK(1, "약함"),
    MEDIUM(2, "보통"),
    STRONG(3, "강함"),
    VERY_STRONG(4, "매우 강함")
}