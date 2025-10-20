package org.setackle.backend.presentation.security

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException

/**
 * 보안 헤더 추가 필터
 * OWASP에서 권장하는 보안 헤더들을 자동으로 추가
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SecurityHeadersFilter : Filter {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        // CORS 헤더 (개발환경용, 운영환경에서는 더 제한적으로 설정)
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:3000")
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With")
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true")
        httpResponse.setHeader("Access-Control-Max-Age", "3600")

        // 보안 헤더들
        addSecurityHeaders(httpResponse)

        // OPTIONS 요청 처리 (CORS preflight)
        if ("OPTIONS" == httpRequest.method) {
            httpResponse.status = HttpServletResponse.SC_OK
            return
        }

        chain.doFilter(request, response)
    }

    private fun addSecurityHeaders(response: HttpServletResponse) {
        // X-Content-Type-Options: MIME 타입 스니핑 방지
        response.setHeader("X-Content-Type-Options", "nosniff")

        // X-Frame-Options: 클릭재킹 방지
        response.setHeader("X-Frame-Options", "DENY")

        // X-XSS-Protection: XSS 공격 방지 (구형 브라우저용)
        response.setHeader("X-XSS-Protection", "1; mode=block")

        // Referrer-Policy: 레퍼러 정보 제한
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin")

        // Content-Security-Policy: XSS 및 데이터 인젝션 공격 방지
        response.setHeader(
            "Content-Security-Policy",
            "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self'; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none'",
        )

        // Strict-Transport-Security: HTTPS 강제 (HTTPS 환경에서만 설정)
        // 운영환경에서는 활성화 필요
        // response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains")

        // Permissions-Policy: 브라우저 기능 사용 제한
        response.setHeader(
            "Permissions-Policy",
            "camera=(), microphone=(), geolocation=(), payment=(), usb=()",
        )

        // Cache-Control: 민감한 정보 캐싱 방지 (API 응답용)
        if (isApiRequest(response)) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private")
            response.setHeader("Pragma", "no-cache")
            response.setHeader("Expires", "0")
        }
    }

    private fun isApiRequest(response: HttpServletResponse): Boolean {
        val contentType = response.contentType
        return contentType != null && contentType.contains("application/json")
    }

    override fun init(filterConfig: FilterConfig?) {
        // 필터 초기화 로직 (필요시)
    }

    override fun destroy() {
        // 필터 정리 로직 (필요시)
    }
}
