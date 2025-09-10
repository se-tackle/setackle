package org.setackle.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
