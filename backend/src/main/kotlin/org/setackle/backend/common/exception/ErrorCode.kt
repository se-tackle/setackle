package org.setackle.backend.common.exception

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus

@Schema(description = "비즈니스 오류 코드")
enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력 값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A003", "인증에 실패했습니다."),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A004", "이메일 또는 비밀번호가 일치하지 않습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),

    // Resource
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "요청한 리소스를 찾을 수 없습니다.")
}