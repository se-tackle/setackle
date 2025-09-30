package org.setackle.backend.adapter.web.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.regex.Pattern

/**
 * 요청 검증 인터셉터
 * 악성 입력값과 보안 위협을 사전에 차단
 */
@Component
class RequestValidationInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(RequestValidationInterceptor::class.java)

    companion object {
        // SQL Injection 패턴
        private val SQL_INJECTION_PATTERNS = listOf(
            Pattern.compile("('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(union|select|insert|delete|update|drop|create|alter|exec|execute)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(script|javascript|vbscript|onload|onerror|onclick)", Pattern.CASE_INSENSITIVE)
        )

        // XSS 패턴
        private val XSS_PATTERNS = listOf(
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        )

        // 경로 순회 공격 패턴
        private val PATH_TRAVERSAL_PATTERNS = listOf(
            Pattern.compile("\\.\\./"),
            Pattern.compile("\\.\\.\\\\"),
            Pattern.compile("%2e%2e%2f", Pattern.CASE_INSENSITIVE),
            Pattern.compile("%2e%2e%5c", Pattern.CASE_INSENSITIVE)
        )

        // 과도하게 긴 입력값 제한
        private const val MAX_HEADER_LENGTH = 8192
        private const val MAX_PARAMETER_LENGTH = 4096
        private const val MAX_URI_LENGTH = 2048
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        try {
            // 1. URI 길이 검증
            if (request.requestURI.length > MAX_URI_LENGTH) {
                return blockRequest(response, "URI가 너무 깁니다.", "INVALID_URI_LENGTH")
            }

            // 2. 경로 순회 공격 검사
            if (containsPathTraversal(request.requestURI)) {
                return blockRequest(response, "유효하지 않은 경로입니다.", "PATH_TRAVERSAL_DETECTED")
            }

            // 3. 헤더 검증
            if (!validateHeaders(request)) {
                return blockRequest(response, "유효하지 않은 헤더입니다.", "INVALID_HEADERS")
            }

            // 4. 쿼리 파라미터 검증
            if (!validateQueryParameters(request)) {
                return blockRequest(response, "유효하지 않은 요청 파라미터입니다.", "INVALID_PARAMETERS")
            }

            // 5. User-Agent 검증
            if (!validateUserAgent(request)) {
                return blockRequest(response, "유효하지 않은 User-Agent입니다.", "INVALID_USER_AGENT")
            }

            return true

        } catch (e: Exception) {
            logger.error("Request validation error", e)
            return blockRequest(response, "요청 처리 중 오류가 발생했습니다.", "VALIDATION_ERROR")
        }
    }

    private fun validateHeaders(request: HttpServletRequest): Boolean {
        val headerNames = request.headerNames

        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement()
            val headerValue = request.getHeader(headerName)

            // 헤더 길이 검증
            if (headerValue.length > MAX_HEADER_LENGTH) {
                logger.warn("Header too long: $headerName")
                return false
            }

            // 헤더 값에서 악성 패턴 검사
            if (containsMaliciousPattern(headerValue)) {
                logger.warn("Malicious pattern detected in header: $headerName = $headerValue")
                return false
            }
        }

        return true
    }

    private fun validateQueryParameters(request: HttpServletRequest): Boolean {
        val parameterMap = request.parameterMap

        for ((paramName, paramValues) in parameterMap) {
            for (paramValue in paramValues) {
                // 파라미터 길이 검증
                if (paramValue.length > MAX_PARAMETER_LENGTH) {
                    logger.warn("Parameter too long: $paramName")
                    return false
                }

                // 파라미터 값에서 악성 패턴 검사
                if (containsMaliciousPattern(paramValue)) {
                    logger.warn("Malicious pattern detected in parameter: $paramName = $paramValue")
                    return false
                }
            }
        }

        return true
    }

    private fun validateUserAgent(request: HttpServletRequest): Boolean {
        val userAgent = request.getHeader("User-Agent")

        if (userAgent.isNullOrBlank()) {
            // User-Agent가 없는 요청도 허용 (일부 정상적인 클라이언트에서 생략할 수 있음)
            return true
        }

        // 과도하게 긴 User-Agent 차단
        if (userAgent.length > 512) {
            logger.warn("User-Agent too long: ${userAgent.take(100)}...")
            return false
        }

        // 알려진 악성 Bot 패턴 검사
        val maliciousBotPatterns = listOf(
            "sqlmap", "nikto", "nmap", "masscan", "nessus",
            "acunetix", "burpsuite", "zaproxy", "dirbuster"
        )

        val userAgentLower = userAgent.lowercase()
        for (pattern in maliciousBotPatterns) {
            if (userAgentLower.contains(pattern)) {
                logger.warn("Malicious bot detected: $userAgent")
                return false
            }
        }

        return true
    }

    private fun containsMaliciousPattern(input: String): Boolean {
        // SQL Injection 패턴 검사
        for (pattern in SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true
            }
        }

        // XSS 패턴 검사
        for (pattern in XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true
            }
        }

        return false
    }

    private fun containsPathTraversal(path: String): Boolean {
        for (pattern in PATH_TRAVERSAL_PATTERNS) {
            if (pattern.matcher(path).find()) {
                return true
            }
        }
        return false
    }

    private fun blockRequest(
        response: HttpServletResponse,
        message: String,
        errorCode: String
    ): Boolean {
        logger.warn("Request blocked: $errorCode - $message")

        response.status = HttpStatus.BAD_REQUEST.value()
        response.contentType = "application/json"
        response.writer.write("""
            {
                "success": false,
                "error": "$errorCode",
                "message": "$message"
            }
        """.trimIndent())

        return false
    }
}