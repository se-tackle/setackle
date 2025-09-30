package org.setackle.backend.adapter.config

import org.setackle.backend.adapter.security.JwtAccessDeniedHandler
import org.setackle.backend.adapter.security.JwtAuthenticationEntryPoint
import org.setackle.backend.adapter.security.JwtAuthenticationFilter
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
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
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
                        "/actuator/prometheus"
                    ).permitAll()

                    // API documentation
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                    ).permitAll()

                    // Authentication endpoints
                    .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh"
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
            addAllowedOriginPattern("*")
            addAllowedHeader("*")
            addAllowedMethod("*")
            allowCredentials = false
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }

        return CorsFilter(source)
    }
}