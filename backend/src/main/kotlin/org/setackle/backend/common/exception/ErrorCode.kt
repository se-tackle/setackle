package org.setackle.backend.common.exception

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus

@Schema(description = "비즈니스 오류 코드")
enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력 값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A003", "인증에 실패했습니다."),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A004", "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A005", "이메일 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "A006", "계정이 잠겼습니다."),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "A007", "비활성화된 계정입니다."),
    TOO_MANY_LOGIN_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "A008", "로그인 시도 횟수를 초과했습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "A009", "이메일 인증이 완료되지 않았습니다."),
    CURRENT_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "A010", "현재 비밀번호가 올바르지 않습니다."),
    PASSWORD_REUSED(HttpStatus.BAD_REQUEST, "A011", "최근에 사용한 비밀번호는 재사용할 수 없습니다."),
    INVALID_RESET_TOKEN(HttpStatus.BAD_REQUEST, "A012", "유효하지 않거나 만료된 재설정 토큰입니다."),
    PASSWORD_RESET_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "A013", "비밀번호 재설정 토큰이 만료되었습니다."),
    TOO_MANY_PASSWORD_RESET_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "A014", "비밀번호 재설정 요청이 너무 많습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "U003", "이미 사용 중인 사용자명입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "U004", "비밀번호가 일치하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U005", "비밀번호 정책을 위반했습니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "U006", "유효하지 않은 이메일 형식입니다."),
    ACCOUNT_DELETION_FAILED(HttpStatus.BAD_REQUEST, "U007", "계정 삭제에 실패했습니다."),
    DELETION_NOT_CONFIRMED(HttpStatus.BAD_REQUEST, "U008", "계정 삭제를 확인하지 않았습니다."),
    CANNOT_DELETE_ADMIN(HttpStatus.FORBIDDEN, "U009", "관리자 계정은 삭제할 수 없습니다."),
    ACCOUNT_ALREADY_DELETED(HttpStatus.CONFLICT, "U010", "이미 삭제된 계정입니다."),

    // Token Management
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "T001", "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "T002", "유효하지 않은 토큰입니다."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "T003", "블랙리스트에 등록된 토큰입니다."),
    TOKEN_MALFORMED(HttpStatus.BAD_REQUEST, "T004", "토큰 형식이 올바르지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "T005", "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "T006", "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "T007", "토큰 생성에 실패했습니다."),
    TOKEN_METADATA_NOT_FOUND(HttpStatus.NOT_FOUND, "T008", "토큰 메타데이터를 찾을 수 없습니다."),
    TOKEN_REFRESH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "T009", "토큰 갱신에 실패했습니다."),
    SECURITY_EVENT_LOGGING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "T010", "보안 이벤트 로깅에 실패했습니다."),
    TOKEN_BLACKLIST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "T011", "토큰 블랙리스트 등록에 실패했습니다."),

    // Resource
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "요청한 리소스를 찾을 수 없습니다."),
}
