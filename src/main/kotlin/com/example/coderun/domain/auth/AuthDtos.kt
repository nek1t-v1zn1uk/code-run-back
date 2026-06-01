package com.example.coderun.domain.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class RegisterRequest(
    @field:NotBlank
    @field:Email
    @field:Size(min = 1, max = 255)
    val email: String = "",

    @field:NotBlank
    @field:Size(min = 8, max = 255)
    val password: String = "",

    @field:NotBlank
    @field:Size(min = 1, max = 32)
    val firstName: String = "",

    @field:Size(min = 1, max = 32)
    val lastName: String? = null,
)
data class LoginRequest(
    @field:NotBlank
    @field:Email
    @field:Size(min = 1, max = 255)
    val email: String = "",

    @field:NotBlank
    @field:Size(min = 8, max = 255)
    val password: String = "",
)

data class RegisterResponse(
    val email: String,
    val firstName: String,
    val lastName: String? = null,
    val createdDate: Instant,
)
data class LoginResponse(
    val email: String,
    val accessToken: String,
    val expireDate: Instant,
    val role: String
)