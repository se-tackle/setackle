package org.setackle.backend.presentation.common

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.setackle.backend.common.exception.ErrorCode
import java.time.LocalDateTime

@Schema(description = "오류 응답")
data class ErrorResponse(
    @Schema(description = "오류 코드")
    val code: String,

    @Schema(description = "오류 메시지")
    val message: String,

    @Schema(description = "오류 발생 시각")
    val timestamp: LocalDateTime = LocalDateTime.now(),

    @Schema(description = "필드별 유효성 검사 오류 등 상세 정보")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val details: Map<String, Any?> = emptyMap()
) {
    companion object {
        fun of(errorCode: ErrorCode, message: String): ErrorResponse =
            ErrorResponse(errorCode.code, message)

        fun of(errorCode: ErrorCode, message: String, details: Map<String, Any?>): ErrorResponse =
            ErrorResponse(errorCode.code, message, LocalDateTime.now(), details)
    }
}
