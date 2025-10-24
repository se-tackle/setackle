package org.setackle.backend.domain.skill.vo

/**
 * 로드맵 토픽 ID 값 객체
 *
 * 토픽 식별자를 위한 타입 안전 래퍼
 * - @JvmInline value class로 제로 오버헤드 구현
 * - 타입 안전성 보장 (Long과 혼동 방지)
 * - 양수 값만 허용
 *
 * @property value 토픽 ID 값 (양수)
 */
@JvmInline
value class TopicId(val value: Long) {
    init {
        require(value > 0) { "토픽 ID는 양수여야 합니다: $value" }
    }

    companion object {
        /**
         * Long 값으로부터 TopicId 생성
         *
         * @param value 토픽 ID 값
         * @return TopicId 인스턴스
         * @throws IllegalArgumentException value가 0 이하인 경우
         */
        fun of(value: Long): TopicId = TopicId(value)

        /**
         * Nullable Long 값으로부터 안전하게 TopicId 생성
         *
         * @param value 토픽 ID 값 (null 가능)
         * @return TopicId 인스턴스 또는 null
         */
        fun ofOrNull(value: Long?): TopicId? {
            return value?.takeIf { it > 0 }?.let { TopicId(it) }
        }

        /**
         * 신규 토픽 생성 시 사용 (DB 저장 전)
         *
         * @return null (ID는 DB에서 자동 생성)
         */
        fun newTopic(): TopicId? = null
    }

    override fun toString(): String = value.toString()
}
