package org.setackle.backend.presentation.common

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "API 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    @Schema(description = "응답 데이터 (성공 시)")
    val data: T?,

    @Schema(description = "오류 정보 (실패 시)")
    val error: ErrorResponse? = null,

    @Schema(description = "응답 시간")
    val timestamp: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(data = data)
        }

        fun success(): ApiResponse<Unit> {
            return ApiResponse(data = Unit)
        }

        fun <T> error(errorResponse: ErrorResponse): ApiResponse<T> {
            return ApiResponse(data = null, error = errorResponse)
        }
    }
}
