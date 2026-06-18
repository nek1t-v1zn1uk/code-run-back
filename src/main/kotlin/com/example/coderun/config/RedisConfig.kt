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
        // Use default constructor — it includes @class type metadata in JSON,
        // which avoids Spring DevTools RestartClassLoader conflicts.
        val jsonSerializer = GenericJackson2JsonRedisSerializer()

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
