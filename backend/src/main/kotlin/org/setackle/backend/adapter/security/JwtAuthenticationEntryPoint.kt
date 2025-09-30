package org.setackle.backend.adapter.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.setackle.backend.adapter.web.common.ErrorResponse
import org.setackle.backend.common.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint::class.java)

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.error("인증되지 않은 접근 시도: {}", authException.message)

        val errorResponse = ErrorResponse.of(
            ErrorCode.UNAUTHORIZED,
            "인증이 필요합니다. 유효한 토큰을 제공해주세요."
        )

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}