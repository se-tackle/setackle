package org.setackle.backend.domain.user.vo

/**
 * 사용자 ID 값 객체
 * 사용자 식별자의 타입 안정성을 보장하는 불변 값 객체
 */
@JvmInline
value class UserId(val value: Long) {

    init {
        require(value > 0) { "사용자 ID는 양수여야 합니다." }
    }

    override fun toString(): String = value.toString()

    companion object {
        /**
         * UserId 생성 팩토리 메소드
         */
        fun of(id: Long): UserId = UserId(id)

        /**
         * 안전한 UserId 생성 메소드
         * 유효하지 않은 값일 경우 null 반환
         */
        fun ofOrNull(id: Long?): UserId? {
            return try {
                id?.takeIf { it > 0 }?.let(::UserId)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        /**
         * 새로운 사용자를 위한 임시 ID (DB 생성 전)
         */
        fun newUser(): UserId? = null
    }
}