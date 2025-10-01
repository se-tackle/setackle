package org.setackle.backend.application.user.outbound

/**
 * 비밀번호 인코딩 출력 포트
 * Infrastructure Layer에서 구현됨
 *
 * 헥사고날 아키텍처의 Outbound Port로서
 * Application Layer가 Infrastructure의 비밀번호 해싱 기술에 의존하지 않도록 추상화
 */
interface PasswordEncoderPort {
    /**
     * 원시 비밀번호를 해시화
     *
     * @param rawPassword 원시 비밀번호
     * @return 해시된 비밀번호
     */
    fun encode(rawPassword: String): String

    /**
     * 원시 비밀번호와 해시값 매칭 검증
     *
     * @param rawPassword 원시 비밀번호
     * @param encodedPassword 해시된 비밀번호
     * @return 매칭 여부
     */
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
