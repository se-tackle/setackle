package org.setackle.backend.application.user.inbound

/**
 * 로그아웃 유스케이스
 */
interface LogoutUseCase {

    /**
     * 사용자 로그아웃을 수행합니다.
     *
     * @param command 로그아웃 커맨드
     * @return 로그아웃 결과
     * @throws BusinessException 로그아웃 처리 중 오류 발생 시
     */
    fun logout(command: LogoutCommand): LogoutResult
}

/**
 * 로그아웃 커맨드
 *
 * @property userId 사용자 ID
 * @property bearerToken Bearer 토큰 (Authorization 헤더 값)
 */
data class LogoutCommand(
    val userId: Long,
    val bearerToken: String? = null
)

/**
 * 로그아웃 결과
 *
 * @property success 성공 여부
 * @property message 결과 메시지
 */
data class LogoutResult(
    val success: Boolean,
    val message: String
)