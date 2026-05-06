package com.example.coderun.config

import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(responseCode = "400", description = "Validation error")
annotation class ValidatesInput
