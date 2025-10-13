package org.setackle.backend.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
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
        val lettuceConnectionFactory = LettuceConnectionFactory(redisHost, redisPort)
        if (redisPassword.isNotBlank()) {
            lettuceConnectionFactory.setPassword(redisPassword)
        }
        return lettuceConnectionFactory
    }

    @Bean
    fun redisObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            // Java 8 날짜/시간 타입 지원 모듈 등록
            registerModule(JavaTimeModule())
            // Kotlin 지원 모듈 등록
            registerKotlinModule()
            // 날짜를 타임스탬프가 아닌 ISO-8601 형식으로 직렬화
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // 타입 정보 포함 설정 (역직렬화 시 타입 안전성 보장)
            activateDefaultTyping(
                polymorphicTypeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
            )
        }
    }

    @Bean
    fun redisTemplate(
        connectionFactory: RedisConnectionFactory,
        redisObjectMapper: ObjectMapper,
    ): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory

        // Key serializer
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()

        // Value serializer - Java 8 날짜/시간 타입을 지원하는 커스텀 ObjectMapper 사용
        val serializer = GenericJackson2JsonRedisSerializer(redisObjectMapper)
        template.valueSerializer = serializer
        template.hashValueSerializer = serializer

        template.afterPropertiesSet()
        return template
    }
}
