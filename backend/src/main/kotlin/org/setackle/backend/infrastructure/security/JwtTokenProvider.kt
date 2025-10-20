package org.setackle.backend.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.setackle.backend.domain.user.vo.UserRole
import org.setackle.backend.infrastructure.config.JwtConfig
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtConfig: JwtConfig,
) {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * Access Token 생성
     */
    fun generateAccessToken(authentication: Authentication): String {
        val userDetails = authentication.principal as CustomUserDetails
        val now = Date()
        val expiryDate = Date(now.time + jwtConfig.accessTokenValidity * 1000)

        return Jwts.builder()
            .subject(userDetails.getUserId().toString())
            .claim("email", userDetails.getEmail())
            .claim("username", userDetails.getUsernameDisplay())
            .claim("role", userDetails.getRole().name)
            .claim("type", "ACCESS")
            .issuedAt(now)
            .expiration(expiryDate)
            .issuer(jwtConfig.issuer)
            .signWith(secretKey, Jwts.SIG.HS512)
            .compact()
    }

    /**
     * Refresh Token 생성
     */
    fun generateRefreshToken(authentication: Authentication): String {
        val userDetails = authentication.principal as CustomUserDetails
        val now = Date()
        val expiryDate = Date(now.time + jwtConfig.refreshTokenValidity * 1000)

        return Jwts.builder()
            .subject(userDetails.getUserId().toString())
            .claim("type", "REFRESH")
            .issuedAt(now)
            .expiration(expiryDate)
            .issuer(jwtConfig.issuer)
            .signWith(secretKey, Jwts.SIG.HS512)
            .compact()
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): Long? {
        return try {
            val claims = getClaimsFromToken(token)
            claims.subject.toLongOrNull()
        } catch (e: Exception) {
            logger.error("토큰에서 사용자 ID를 추출하는데 실패했습니다.", e)
            null
        }
    }

    /**
     * 토큰에서 이메일 추출
     */
    fun getEmailFromToken(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            claims["email"] as? String
        } catch (e: Exception) {
            logger.error("토큰에서 이메일을 추출하는데 실패했습니다.", e)
            null
        }
    }

    /**
     * 토큰에서 역할 추출
     */
    fun getRoleFromToken(token: String): UserRole? {
        return try {
            val claims = getClaimsFromToken(token)
            val roleString = claims["role"] as? String
            roleString?.let { UserRole.valueOf(it) }
        } catch (e: Exception) {
            logger.error("토큰에서 역할을 추출하는데 실패했습니다.", e)
            null
        }
    }

    /**
     * 토큰에서 만료일 추출
     */
    fun getExpirationFromToken(token: String): LocalDateTime? {
        return try {
            val claims = getClaimsFromToken(token)
            claims.expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        } catch (e: Exception) {
            logger.error("토큰에서 만료일을 추출하는데 실패했습니다.", e)
            null
        }
    }

    /**
     * 토큰 타입 확인 (ACCESS, REFRESH)
     */
    fun getTokenType(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            claims["type"] as? String
        } catch (e: Exception) {
            logger.error("토큰 타입을 확인하는데 실패했습니다.", e)
            null
        }
    }

    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            !claims.expiration.before(Date())
        } catch (e: JwtException) {
            logger.error("JWT 토큰 검증 실패: ${e.message}")
            false
        } catch (e: IllegalArgumentException) {
            logger.error("JWT 토큰이 비어있습니다.", e)
            false
        } catch (e: Exception) {
            logger.error("JWT 토큰 검증 중 예외 발생", e)
            false
        }
    }

    /**
     * Access Token 여부 확인
     */
    fun isAccessToken(token: String): Boolean {
        return getTokenType(token) == "ACCESS"
    }

    /**
     * Refresh Token 여부 확인
     */
    fun isRefreshToken(token: String): Boolean {
        return getTokenType(token) == "REFRESH"
    }

    /**
     * 토큰에서 Claims 추출
     */
    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * HTTP 요청에서 토큰 추출
     */
    fun resolveToken(bearerToken: String?): String? {
        return if (bearerToken != null && bearerToken.startsWith(jwtConfig.tokenPrefix)) {
            bearerToken.substring(jwtConfig.tokenPrefix.length)
        } else {
            null
        }
    }
}
