package com.example.coderun.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springdoc.core.customizers.OpenApiCustomizer

@Configuration
@OpenAPIDefinition(
    info = Info(title = "CodeRun API", version = "v1.0", description = "API documentation for CodeRun Backend."),
    security = [SecurityRequirement(name = "bearerAuth")],
    tags = [
        Tag(name = "Server Healthcheck"),
    ]
)
@SecurityScheme(
    name = "bearerAuth",
    description = "Enter JWT, SKIP prefix 'Bearer '",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    `in` = SecuritySchemeIn.HEADER
)
class OpenApiConfig

@Configuration
class SwaggerTagOrderConfig {

    @Bean
    fun reorderTags(): OpenApiCustomizer {
        val tagOrder = listOf(
            "Server Healthcheck",
        )

        return OpenApiCustomizer { openApi ->
            val currentTags = openApi.tags ?: return@OpenApiCustomizer
            openApi.tags = currentTags.sortedBy { tag ->
                tagOrder.indexOf(tag.name).takeIf { it >= 0 } ?: Int.MAX_VALUE
            }
        }
    }

    /*@Bean
    fun addHttpsServer(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi ->
            val servers = openApi.servers ?: mutableListOf()

            servers.add(0, Server().url("https://team-room-jitsi.duckdns.org"))
            servers.add(1, Server().url("http://localhost:8081"))

            openApi.servers = servers
        }
    }*/
}