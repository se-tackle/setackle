package org.setackle.backend.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.presentation.common.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {

    private val logger = LoggerFactory.getLogger(JwtAccessDeniedHandler::class.java)

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        logger.warn("접근 권한이 없는 요청: {} {}", request.method, request.requestURI)

        val errorResponse = ErrorResponse.of(
            ErrorCode.FORBIDDEN,
            "접근 권한이 없습니다. 필요한 권한을 확인해주세요.",
        )

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.status = HttpServletResponse.SC_FORBIDDEN

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
