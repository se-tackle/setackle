package org.setackle.backend.infrastructure.security

import org.setackle.backend.application.user.outbound.PasswordEncoderPort
import org.springframework.stereotype.Component
import org.springframework.security.crypto.password.PasswordEncoder as SpringPasswordEncoder

/**
 * BCrypt 기반 비밀번호 인코더 어댑터
 *
 * Spring Security의 PasswordEncoder를 Application Layer의 Port 인터페이스로 래핑
 * 헥사고날 아키텍처의 Adapter 패턴 적용
 *
 * @property springPasswordEncoder Spring Security에서 제공하는 BCryptPasswordEncoder Bean
 */
@Component
class BCryptPasswordEncoderAdapter(
    private val springPasswordEncoder: SpringPasswordEncoder,
) : PasswordEncoderPort {

    override fun encode(rawPassword: String): String {
        return springPasswordEncoder.encode(rawPassword)
    }

    override fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return springPasswordEncoder.matches(rawPassword, encodedPassword)
    }
}
