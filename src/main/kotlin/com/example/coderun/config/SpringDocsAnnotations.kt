package com.example.coderun.config

import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(responseCode = "400", description = "Validation error")
annotation class ValidatesInput


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('ADMIN')")
@ApiResponse(responseCode = "403", description = "Only ADMIN allowed")
annotation class AdminOnly
