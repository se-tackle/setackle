package org.setackle.backend.infrastructure.config

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

/**
 * 쿠키 설정
 *
 * application.yml의 app.security.cookie 섹션에서 값을 로드합니다.
 * Refresh Token과 같은 인증 정보를 안전하게 쿠키로 전송하기 위한 설정입니다.
 *
 * @property secure HTTPS에서만 쿠키 전송 여부 (프로덕션: true, 개발: false)
 * @property sameSite SameSite 정책 (Strict/Lax/None) - CSRF 방어
 * @property domain 쿠키 도메인 (프로덕션 환경에서만 설정)
 * @property maxAgeDays 쿠키 유효기간 (일 단위)
 */
@Configuration
@ConfigurationProperties(prefix = "app.security.cookie")
@Validated
data class CookieProperties(
    /**
     * HTTPS에서만 쿠키 전송 여부
     * - true: HTTPS에서만 전송 (프로덕션 권장)
     * - false: HTTP에서도 전송 (로컬 개발 환경)
     */
    var secure: Boolean = false,

    /**
     * SameSite 정책 (CSRF 방어)
     * - Strict: 동일 사이트에서만 쿠키 전송
     * - Lax: 안전한 HTTP 메서드(GET 등)는 크로스 사이트 허용
     * - None: 모든 크로스 사이트 요청 허용 (secure=true 필수)
     */
    @field:NotBlank(message = "SameSite 정책은 필수입니다")
    var sameSite: String = "",

    /**
     * 쿠키 도메인
     * - 비어있으면: 현재 도메인만
     * - 설정 시: 서브도메인 포함
     */
    var domain: String = "",

    /**
     * 쿠키 유효기간 (일 단위)
     * Refresh Token 유효기간과 동일하게 설정 권장
     */
    @field:Min(value = 1, message = "쿠키 유효기간은 최소 1일 이상이어야 합니다")
    var maxAgeDays: Long = 0,
)
