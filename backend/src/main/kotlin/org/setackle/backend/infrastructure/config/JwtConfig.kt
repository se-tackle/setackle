package org.setackle.backend.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
data class JwtConfig(
    var secret: String = "default-secret-key-change-in-production",
    var accessTokenValidity: Long = 900, // 15분 (초 단위)
    var refreshTokenValidity: Long = 604800, // 7일 (초 단위)
    var issuer: String = "setackle",
    var header: String = "Authorization",
    var tokenPrefix: String = "Bearer "
)