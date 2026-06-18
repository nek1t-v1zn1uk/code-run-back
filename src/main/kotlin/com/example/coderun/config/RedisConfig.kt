package com.example.coderun.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig {

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        val mapper = com.fasterxml.jackson.databind.ObjectMapper().apply {
            registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            registerModule(com.fasterxml.jackson.module.kotlin.KotlinModule.Builder().build())
            activateDefaultTyping(
                com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance,
                com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
            )
        }
        val jsonSerializer = GenericJackson2JsonRedisSerializer(mapper)

        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
            )
            .disableCachingNullValues()

        val cacheConfigurations = mapOf(
            "scoreboard" to defaultConfig.entryTtl(Duration.ofSeconds(30)),
            "contest-problems" to defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "contest-details" to defaultConfig.entryTtl(Duration.ofMinutes(2))
        )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(1)))
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}
