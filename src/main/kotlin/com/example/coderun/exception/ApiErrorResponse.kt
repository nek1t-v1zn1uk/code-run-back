package com.example.coderun.exception

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(name = "ApiErrorResponse", description = "Standard error structure")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiErrorResponse(
    val status: Int,
    val message: String?,
    val timestamp: Instant = Instant.now(),
    val errors: Map<String, String>? = null
)