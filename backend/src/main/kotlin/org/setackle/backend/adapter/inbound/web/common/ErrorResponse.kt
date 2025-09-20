package org.setackle.backend.adapter.inbound.web.common

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "오류 응답")
data class ErrorResponse(
    @Schema(description = "오류 코드")
    val code: String,

    @Schema(description = "오류 메시지")
    val message: String,

    @Schema(description = "필드별 유효성 검사 오류 등 상세 정보")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val details: Map<String, Any?> = emptyMap()
)
