package org.setackle.backend.adapter.inbound.web.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.setackle.backend.application.user.outbound.TokenCachePort
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

/**
 * Rate Limiting 인터셉터
 * API 호출 횟수를 제한하여 브루트 포스 공격을 방지
 */
@Component
class RateLimitingInterceptor(
    private val tokenCachePort: TokenCachePort
) : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(RateLimitingInterceptor::class.java)

    companion object {
        // 엔드포인트별 제한 설정
        private val RATE_LIMITS = mapOf(
            "/api/auth/login" to RateLimit(maxAttempts = 5, windowMinutes = 15),
            "/api/auth/register" to RateLimit(maxAttempts = 3, windowMinutes = 60),
            "/api/auth/forgot-password" to RateLimit(maxAttempts = 3, windowMinutes = 60),
            "/api/auth/reset-password" to RateLimit(maxAttempts = 3, windowMinutes = 60),
            "/api/auth/change-password" to RateLimit(maxAttempts = 10, windowMinutes = 60)
        )

        private const val RATE_LIMIT_PREFIX = "rate_limit:"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val endpoint = request.requestURI
        val rateLimit = RATE_LIMITS[endpoint] ?: return true // 제한이 없는 엔드포인트는 통과

        val clientKey = generateClientKey(request, endpoint)
        val currentAttempts = getCurrentAttempts(clientKey, rateLimit.windowMinutes)

        if (currentAttempts >= rateLimit.maxAttempts) {
            logger.warn("Rate limit exceeded: endpoint=$endpoint, clientKey=$clientKey, attempts=$currentAttempts")

            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write("""
                {
                    "success": false,
                    "error": "TOO_MANY_REQUESTS",
                    "message": "요청 횟수 제한을 초과했습니다. ${rateLimit.windowMinutes}분 후에 다시 시도해주세요.",
                    "retryAfter": ${rateLimit.windowMinutes * 60}
                }
            """.trimIndent())

            return false
        }

        // 시도 횟수 증가
        incrementAttempts(clientKey, rateLimit.windowMinutes)

        return true
    }

    private fun generateClientKey(request: HttpServletRequest, endpoint: String): String {
        val ipAddress = extractIpAddress(request)
        val userAgent = request.getHeader("User-Agent") ?: "unknown"
        val userAgentHash = userAgent.hashCode()

        return "${ipAddress}_${endpoint}_$userAgentHash"
    }

    private fun extractIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        val xRealIp = request.getHeader("X-Real-IP")

        return when {
            !xForwardedFor.isNullOrBlank() -> xForwardedFor.split(",")[0].trim()
            !xRealIp.isNullOrBlank() -> xRealIp
            else -> request.remoteAddr ?: "unknown"
        }
    }

    private fun getCurrentAttempts(clientKey: String, windowMinutes: Int): Int {
        return try {
            val key = "$RATE_LIMIT_PREFIX$clientKey"
            // Redis에서 현재 시도 횟수 조회
            // 실제 구현에서는 tokenCachePort를 확장하여 카운터 기능 추가 필요
            // 현재는 기본값 0 반환
            0
        } catch (e: Exception) {
            logger.error("Failed to get current attempts for key: $clientKey", e)
            0
        }
    }

    private fun incrementAttempts(clientKey: String, windowMinutes: Int) {
        try {
            val key = "$RATE_LIMIT_PREFIX$clientKey"
            val ttl = Duration.ofMinutes(windowMinutes.toLong())

            // Redis에서 카운터 증가
            // 실제 구현에서는 tokenCachePort를 확장하여 INCR 명령 지원 필요
            // 현재는 로그만 남김
            logger.debug("Incrementing attempts for key: $clientKey")

        } catch (e: Exception) {
            logger.error("Failed to increment attempts for key: $clientKey", e)
        }
    }

    /**
     * Rate Limit 설정
     */
    data class RateLimit(
        val maxAttempts: Int,      // 최대 시도 횟수
        val windowMinutes: Int     // 시간 창 (분)
    )
}