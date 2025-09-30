package org.setackle.backend.application.user.outbound

import org.setackle.backend.domain.user.model.User
import java.time.LocalDateTime

/**
 * 토큰 관련 출력 포트 (Output Port)
 * Domain에서 정의하고 Adapter에서 구현
 */
interface TokenPort {

    /**
     * 토큰 생성
     */
    fun generateAccessToken(user: User): String
    fun generateRefreshToken(user: User): String

    /**
     * 토큰 검증
     */
    fun validateToken(token: String): Boolean
    fun isAccessToken(token: String): Boolean
    fun isRefreshToken(token: String): Boolean

    /**
     * 토큰 정보 추출
     */
    fun getUserIdFromToken(token: String): Long?
    fun getEmailFromToken(token: String): String?
    fun getExpirationFromToken(token: String): LocalDateTime?

    /**
     * 토큰 해석
     */
    fun resolveToken(bearerToken: String?): String?
}
