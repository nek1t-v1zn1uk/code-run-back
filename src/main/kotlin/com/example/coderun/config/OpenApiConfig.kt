package com.example.coderun.config

import com.example.coderun.exception.ApiErrorResponse
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(title = "CodeRun API", version = "v1.0", description = "API documentation for CodeRun Backend."),
    security = [SecurityRequirement(name = "bearerAuth")],
    tags = [
        Tag(name = "Server Healthcheck"),
        Tag(name = "Authentication"),
        Tag(name = "Code Execution"),
        Tag(name = "Problems"),
        Tag(name = "Solutions"),
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
class OpenApiConfig {

    @Bean
    fun globalResponseCustomizer(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi ->
            openApi.paths.values.forEach { pathItem ->
                pathItem.readOperations().forEach { operation ->
                    val responses = operation.responses

                    // Add 500 Internal Server Error if missing
                    if (!responses.containsKey("500")) {
                        responses.addApiResponse("500", ApiResponse()
                            .description("Internal Server Error")
                            .content(
                                Content().addMediaType(
                                "application/json",
                                MediaType().schema(Schema<Any>().`$ref`("#/components/schemas/ApiErrorResponse"))
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    @Bean
    fun errorResponseCustomizer(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi ->
            if (openApi.components == null) {
                openApi.components = Components()
            }

            // Manually register ApiErrorResponse so it appears in the schemas
            io.swagger.v3.core.converter.ModelConverters.getInstance()
                .readAll(ApiErrorResponse::class.java)
                .forEach { name, schema -> openApi.components.addSchemas(name, schema) }

            val errorSchemaRef = Schema<Any>().`$ref`("#/components/schemas/ApiErrorResponse")
            val errorContent = Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                MediaType().schema(errorSchemaRef)
            )

            openApi.paths.values.forEach { pathItem ->
                pathItem.readOperations().forEach { operation ->
                    operation.responses.forEach { (statusCode, response) ->
                        // Apply ApiErrorResponse for 4xx/5xx
                        if (statusCode.startsWith("4") || statusCode.startsWith("5")) {
                            response.content = errorContent
                        }
                    }
                }
            }
        }
    }
}

@Configuration
class SwaggerTagOrderConfig {

    @Bean
    fun reorderTags(): OpenApiCustomizer {
        val tagOrder = listOf(
            "Server Healthcheck",
            "Authentication",
            "Code Execution",
            "Problems",
            "Solutions"
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