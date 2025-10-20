package org.setackle.backend.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.setackle.backend.application.user.outbound.TokenCachePort
import org.setackle.backend.infrastructure.config.JwtConfig
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: CustomUserDetailsService,
    private val jwtConfig: JwtConfig,
    private val tokenCachePort: TokenCachePort,
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val jwt = getJwtFromRequest(request)

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                if (tokenCachePort.isTokenBlacklisted(jwt)) {
                    logger.warn("블랙리스트에 등록된 토큰으로 요청됨")
                    filterChain.doFilter(request, response)
                    return
                }

                if (!jwtTokenProvider.isAccessToken(jwt)) {
                    logger.warn("Access Token이 아닌 토큰으로 요청됨")
                    filterChain.doFilter(request, response)
                    return
                }

                val userId = jwtTokenProvider.getUserIdFromToken(jwt)
                if (userId != null) {
                    try {
                        val userDetails = userDetailsService.loadUserByUserId(userId)

                        if (userDetails.isEnabled) {
                            val authentication = UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.authorities,
                            )
                            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                            SecurityContextHolder.getContext().authentication = authentication

                            logger.debug("사용자 인증 성공: userId=$userId, email=${userDetails.username}")
                        } else {
                            logger.warn("비활성화되거나 이메일 미인증 사용자: userId=$userId")
                        }
                    } catch (e: UsernameNotFoundException) {
                        logger.warn("토큰의 사용자 ID를 찾을 수 없음: userId=$userId")
                    }
                }
            } else if (jwt != null) {
                logger.warn("유효하지 않은 JWT 토큰")
            }
        } catch (e: Exception) {
            logger.error("JWT 인증 필터에서 예외 발생", e)
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(jwtConfig.header)
        return jwtTokenProvider.resolveToken(bearerToken)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI

        val excludePaths = listOf(
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
        )

        return excludePaths.any { path.startsWith(it) }
    }
}
