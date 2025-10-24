package org.setackle.backend.domain.skill.vo

/**
 * 로드맵 URL 슬러그 값 객체
 *
 * 로드맵 관련 값 객체
 * - URL에 사용되는 로드맵 식별자
 * - 소문자, 숫자, 하이픈만 허용
 * - 자동 정규화 (공백 제거, 소문자 변환, 특수문자 치환)
 *
 * 예시: "Frontend Development" -> "frontend-development"
 */
@JvmInline
value class RoadmapSlug private constructor(val value: String) {
    init {
        require(value.isNotBlank()) { "슬러그는 비어있을 수 없습니다" }
        require(value.length <= MAX_LENGTH) { "슬러그는 ${MAX_LENGTH}자를 초과할 수 없습니다: ${value.length}" }
        require(VALID_PATTERN.matches(value)) {
            "슬러그는 소문자, 숫자, 하이픈만 허용됩니다: $value"
        }
    }

    /**
     * 슬러그가 유효한 형식인지 확인
     */
    fun isValid(): Boolean {
        return value.isNotBlank() &&
            value.length <= MAX_LENGTH &&
            VALID_PATTERN.matches(value)
    }

    /**
     * 슬러그의 단어 개수 계산
     */
    fun wordCount(): Int = value.split("-").count { it.isNotBlank() }

    companion object {
        private const val MAX_LENGTH = 100
        private val VALID_PATTERN = "^[a-z0-9-]+$".toRegex()

        // 예약어 목록 (시스템 경로와 충돌 방지)
        private val RESERVED_SLUGS = setOf(
            "admin", "api", "auth", "login", "logout", "register",
            "profile", "settings", "dashboard", "help", "about",
            "terms", "privacy", "contact", "new", "edit", "delete",
        )

        /**
         * 문자열로부터 RoadmapSlug 생성 (자동 정규화)
         *
         * @param value 원본 문자열
         * @return 정규화된 RoadmapSlug
         * @throws IllegalArgumentException 유효하지 않은 슬러그인 경우
         */
        fun of(value: String): RoadmapSlug {
            val normalized = normalize(value)
            require(!RESERVED_SLUGS.contains(normalized)) {
                "예약된 슬러그는 사용할 수 없습니다: $normalized"
            }
            return RoadmapSlug(normalized)
        }

        /**
         * 안전한 RoadmapSlug 생성 (실패 시 null)
         *
         * @param value 원본 문자열
         * @return 정규화된 RoadmapSlug 또는 null
         */
        fun ofOrNull(value: String?): RoadmapSlug? {
            return value?.let {
                runCatching { of(it) }.getOrNull()
            }
        }

        /**
         * 문자열 정규화
         * - 공백 제거
         * - 소문자 변환
         * - 특수문자를 하이픈으로 치환
         * - 연속된 하이픈 제거
         * - 앞뒤 하이픈 제거
         */
        private fun normalize(value: String): String {
            return value
                .trim()
                .lowercase()
                .replace(Regex("\\s+"), "-") // 공백을 하이픈으로
                .replace(Regex("[^a-z0-9-]"), "-") // 허용되지 않는 문자를 하이픈으로
                .replace(Regex("-+"), "-") // 연속된 하이픈을 하나로
                .trim('-') // 앞뒤 하이픈 제거
        }

        /**
         * 슬러그 유효성 검증 (정규화 없이)
         *
         * @param value 검증할 문자열
         * @return 유효한 슬러그인지 여부
         */
        fun isValidSlug(value: String): Boolean {
            return value.isNotBlank() &&
                value.length <= MAX_LENGTH &&
                VALID_PATTERN.matches(value) &&
                !RESERVED_SLUGS.contains(value)
        }
    }

    override fun toString(): String = value
}
