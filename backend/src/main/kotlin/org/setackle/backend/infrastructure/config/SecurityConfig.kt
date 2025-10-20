package org.setackle.backend.infrastructure.config

import org.setackle.backend.infrastructure.security.JwtAccessDeniedHandler
import org.setackle.backend.infrastructure.security.JwtAuthenticationEntryPoint
import org.setackle.backend.infrastructure.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            }
            .authorizeHttpRequests { auth ->
                auth
                    // Health check endpoints
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/actuator/metrics",
                        "/actuator/prometheus",
                    ).permitAll()
                    // API documentation
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                    ).permitAll()
                    // Authentication endpoints
                    .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                    ).permitAll()
                    // All other requests require authentication
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration().apply {
            // allowCredentials = true일 때는 명시적인 origin 지정 필요 (패턴 불가)
            addAllowedOrigin("http://localhost:3000") // 프론트엔드
            addAllowedOrigin("http://localhost:8080") // Swagger UI
            addAllowedHeader("*")
            addAllowedMethod("*")
            allowCredentials = true // Cookie 전송을 위해 필수!
            exposedHeaders = listOf("Set-Cookie") // Set-Cookie 헤더 노출
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }

        return CorsFilter(source)
    }
}
