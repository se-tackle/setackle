package org.setackle.backend.infrastructure.config

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

/**
 * JWT 설정
 *
 * application.yml의 jwt 섹션에서 값을 로드합니다.
 * 모든 필드는 YML 파일에 정의되어 있어야 합니다 (기본값 없음).
 *
 * @property secret JWT 서명 키 (최소 256비트 권장)
 * @property accessTokenValidity Access Token 유효기간 (초)
 * @property refreshTokenValidity Refresh Token 유효기간 (초)
 * @property issuer JWT 발행자
 * @property header Authorization 헤더 이름
 * @property tokenPrefix 토큰 접두사 (예: "Bearer ")
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
data class JwtConfig(
      /**
       * JWT 서명 키 (필수)
       * YML에 반드시 정의되어 있어야 합니다.
       */
      @field:NotBlank(message = "JWT secret은 필수입니다")
      var secret: String = "",

      /**
       * Access Token 유효기간 (초 단위, 필수)
       * 최소 1초 이상이어야 합니다.
       */
      @field:Min(value = 1, message = "Access Token 유효기간은 최소 1초 이상이어야 합니다")
      var accessTokenValidity: Long = 0,

      /**
       * Refresh Token 유효기간 (초 단위, 필수)
       * 최소 1초 이상이어야 합니다.
       */
      @field:Min(value = 1, message = "Refresh Token 유효기간은 최소 1초 이상이어야 합니다")
      var refreshTokenValidity: Long = 0,

      /**
       * JWT 발행자 (선택, 기본값: "setackle")
       */
      var issuer: String = "",

      /**
       * Authorization 헤더 이름 (선택, 기본값: "Authorization")
       */
      var header: String = "",

      /**
       * 토큰 접두사 (선택, 기본값: "Bearer ")
       */
      var tokenPrefix: String = "",

)