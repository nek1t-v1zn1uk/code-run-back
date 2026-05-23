package com.example.coderun.domain.solutions.dto

import com.example.coderun.domain.solutions.entity.AvailableLanguage
import com.example.coderun.domain.solutions.entity.SolutionStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class SolutionDto(
    var id: Int,
    var problemId: Int,
    var userId: Int,
    var code: String,
    var language: AvailableLanguage,
    var contestId: Int? = null,
    var contestProblemId: Int? = null,
    var status: SolutionStatus = SolutionStatus.IN_QUEUE,
    var testCaseReached: Int? = null,
    var executionTimeMs: Int? = null,
    var executionMemoryKb: Int? = null,
    var sentAt: Instant = Instant.now(),
    var executedAt: Instant? = null
)

data class SendSolutionRequest(
    @NotBlank
    @Size(max = 50_000)
    val code: String = "",

    @NotBlank
    @Size(max = 32)
    val language: String = "",

    @Size(max = 32)
    val languageVersion: String? = null,

    val contestId: Int? = null,
    val contestProblemId: Int? = null,
)