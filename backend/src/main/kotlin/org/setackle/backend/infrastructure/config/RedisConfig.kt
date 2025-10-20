package org.setackle.backend.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Value("\${spring.data.redis.host:localhost}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port:6379}")
    private var redisPort: Int = 6379

    @Value("\${spring.data.redis.password:}")
    private var redisPassword: String = ""

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisHost, redisPort)
        if (redisPassword.isNotBlank()) {
            config.setPassword(redisPassword)
        }
        return LettuceConnectionFactory(config)
    }

    /**
     * Redis 전용 ObjectMapper 생성
     * Spring MVC의 기본 ObjectMapper와 분리하여 Redis 직렬화에만 사용
     *
     * @return Redis 직렬화용 ObjectMapper (타입 정보 포함)
     */
    private fun createRedisObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            // Java 8 날짜/시간 타입 지원 모듈 등록
            registerModule(JavaTimeModule())
            // Kotlin 지원 모듈 등록
            registerKotlinModule()
            // 날짜를 타임스탬프가 아닌 ISO-8601 형식으로 직렬화
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // 타입 정보 포함 설정 (Redis 역직렬화 시 타입 안전성 보장)
            // 주의: 이 설정은 Redis에만 필요하며, HTTP 응답에는 사용하지 않음
            activateDefaultTyping(
                polymorphicTypeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
            )
        }
    }

    @Bean
    fun redisTemplate(
        connectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory

        // Key serializer
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()

        // Value serializer - Redis 전용 ObjectMapper 사용
        val redisObjectMapper = createRedisObjectMapper()
        val serializer = GenericJackson2JsonRedisSerializer(redisObjectMapper)
        template.valueSerializer = serializer
        template.hashValueSerializer = serializer

        template.afterPropertiesSet()
        return template
    }
}
