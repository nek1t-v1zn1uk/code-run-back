package com.example.coderun.domain.tests.dto

import com.example.coderun.domain.solutions.entity.AvailableLanguage
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class ScriptCheckerDto(
    val id: Int,
    var name: String,
    var language: AvailableLanguage,
    var code: String,
    var createdAt: Instant = Instant.now()
)

data class CreateScriptCheckerRequest(
    @NotBlank
    @Size(max = 255)
    var name: String = "",

    @NotBlank
    @Size(max = 32)
    val language: String = "",

    @Size(max = 32)
    val languageVersion: String? = null,

    @NotBlank
    @Size(max = 50_000)
    val code: String = "",
)