package com.example.coderun.config

import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {
    @Bean
    fun solutionQueue(): Queue {
        return Queue("solution-queue", true)
    }
}
