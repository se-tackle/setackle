package org.setackle.backend.adapter.config

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DatabaseConfig {

    /**
     * 개발 환경용 Flyway 마이그레이션 전략
     * 개발 중에는 clean 후 migrate 실행
     */
    @Bean
    @Profile("dev")
    fun cleanMigrateStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            // 개발 환경에서는 데이터를 초기화하고 다시 마이그레이션
            flyway.clean()
            flyway.migrate()
        }
    }

    /**
     * 스테이징 환경용 Flyway 마이그레이션 전략
     * 스테이징에서는 검증 후 마이그레이션
     */
    @Bean
    @Profile("staging")
    fun stagingMigrateStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            // 스테이징 환경에서는 검증 후 마이그레이션
            flyway.validate()
            flyway.migrate()
        }
    }

    /**
     * 프로덕션 환경용 Flyway 마이그레이션 전략
     * 프로덕션에서는 안전하게 마이그레이션만 실행
     */
    @Bean
    @Profile("prod")
    fun safeMigrateStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            // 프로덕션 환경에서는 마이그레이션만 실행
            flyway.migrate()
        }
    }
}