package org.setackle.backend.adapter.config

import org.setackle.backend.adapter.inbound.web.security.RateLimitingInterceptor
import org.setackle.backend.adapter.inbound.web.security.RequestValidationInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 웹 보안 설정
 * 인터셉터 등록 및 웹 보안 정책 설정
 */
@Configuration
class WebSecurityConfig(
    private val rateLimitingInterceptor: RateLimitingInterceptor,
    private val requestValidationInterceptor: RequestValidationInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        // 요청 검증 인터셉터 (가장 먼저 실행)
        registry.addInterceptor(requestValidationInterceptor)
            .addPathPatterns("/api/**")
            .order(1)

        // Rate Limiting 인터셉터
        registry.addInterceptor(rateLimitingInterceptor)
            .addPathPatterns(
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/forgot-password",
                "/api/auth/reset-password",
                "/api/auth/change-password"
            )
            .order(2)
    }
}