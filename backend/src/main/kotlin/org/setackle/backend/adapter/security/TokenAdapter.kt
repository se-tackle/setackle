package org.setackle.backend.adapter.security

import org.setackle.backend.domain.user.model.User
import org.setackle.backend.application.user.outbound.TokenPort
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 토큰 관련 어댑터 (Port의 구현체)
 * JwtTokenProvider를 사용하여 Domain의 TokenPort를 구현
 */
@Component
class TokenAdapter(
    private val jwtTokenProvider: JwtTokenProvider,
) : TokenPort {

    override fun generateAccessToken(user: User): String {
        val userDetails = CustomUserDetails(user)
        val authentication = createAuthentication(userDetails)
        return jwtTokenProvider.generateAccessToken(authentication)
    }

    override fun generateRefreshToken(user: User): String {
        val userDetails = CustomUserDetails(user)
        val authentication = createAuthentication(userDetails)
        return jwtTokenProvider.generateRefreshToken(authentication)
    }

    override fun validateToken(token: String): Boolean {
        return jwtTokenProvider.validateToken(token)
    }

    override fun isAccessToken(token: String): Boolean {
        return jwtTokenProvider.isAccessToken(token)
    }

    override fun isRefreshToken(token: String): Boolean {
        return jwtTokenProvider.isRefreshToken(token)
    }

    override fun getUserIdFromToken(token: String): Long? {
        return jwtTokenProvider.getUserIdFromToken(token)
    }

    override fun getEmailFromToken(token: String): String? {
        return jwtTokenProvider.getEmailFromToken(token)
    }

    override fun getExpirationFromToken(token: String): LocalDateTime? {
        return jwtTokenProvider.getExpirationFromToken(token)
    }

    override fun resolveToken(bearerToken: String?): String? {
        return jwtTokenProvider.resolveToken(bearerToken)
    }

    /**
     * Authentication 객체 생성 헬퍼 메서드
     */
    private fun createAuthentication(userDetails: CustomUserDetails): Authentication {
        return UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities,
        )
    }
}
