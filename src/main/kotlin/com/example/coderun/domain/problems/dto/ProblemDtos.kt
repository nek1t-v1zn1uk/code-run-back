package com.example.coderun.domain.problems.dto

import com.example.coderun.domain.problems.entity.EvaluationType
import com.example.coderun.domain.problems.entity.ProblemDifficulty
import com.example.coderun.domain.problems.entity.ProblemTopic
import com.fasterxml.jackson.databind.annotation.EnumNaming
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

data class ProblemDto(
    var id: Int,
    var title: String,
    var topic: ProblemTopic? = null,
    var difficulty: ProblemDifficulty,
    var statement: String,
    var executionTimeLimitMs: Int,
    var executionMemoryLimitKb: Int,
    var defaultEvaluationType: EvaluationType? = null,
    var defaultScriptCheckerId: Int? = null,
    var createdAt: Instant
)

data class CreateProblemRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 255)
    val title: String = "",

    @field:Size(min = 3, max = 32)
    val topic: String? = null,

    @field:NotNull
    @field:Schema(implementation = ProblemDifficulty::class)
    val difficulty: ProblemDifficulty?,

    @field:NotBlank
    @field:Size(min = 10, max = 10000)
    val statement: String,

    @field:NotNull
    @field:Min(500)
    @field:Max(10000)
    val executionTimeLimitMs: Int = -1,

    @field:NotNull
    @field:Min(512)
    @field:Max(512*1024)
    val executionMemoryLimitKb: Int = -1,

    @field:NotNull
    @field:Schema(implementation = EvaluationType::class)
    val defaultEvaluationType: EvaluationType?
)
data class UpdateProblemRequest(
    @field:Size(min = 3, max = 255)
    val title: String? = null,

    @field:Size(min = 3, max = 32)
    val topic: String? = null,

    @field:Schema(implementation = ProblemDifficulty::class)
    val difficulty: ProblemDifficulty? = null,

    @field:Size(min = 10, max = 10000)
    val statement: String? = null,

    @field:Min(500)
    @field:Max(10000)
    val executionTimeLimitMs: Int? = null,

    @field:Min(512)
    @field:Max(512*1024)
    val executionMemoryLimitKb: Int? = null,

    @field:Schema(implementation = EvaluationType::class)
    val defaultEvaluationType: EvaluationType? = null
)
data class GetProblemsRequest(
    val difficulty: ProblemDifficulty?,
    val topicName: String?,
    val limit: Int = 10,
    val cursor: String?,
)

data class ProblemPageResponse(
    val content: List<ProblemDto>,
    val hasNext: Boolean,
    val nextCursor: String?,
    val isEmpty: Boolean = content.isEmpty()
)

data class ProblemCursor(
    val lastSeenId: Int,
    val lastSeenDifficulty: Int // Int for the Ordinal value
)
