package com.example.coderun.domain.users

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class UserProfileDto(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String?,
    val photoUrl: String?,
    val role: String,
    val createdAt: Instant
)

data class UpdateProfileRequest(
    @field:NotBlank @field:Size(min = 1, max = 32)
    val firstName: String = "",
    
    @field:Size(min = 1, max = 32)
    val lastName: String? = null
)
