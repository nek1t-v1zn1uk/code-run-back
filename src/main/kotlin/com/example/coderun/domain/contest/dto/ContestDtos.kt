package com.example.coderun.domain.contest.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.Instant

data class ContestDto(
    val id: Int,
    val name: String?,
    val overview: String?,
    val rules: String?,
    val startTime: Instant,
    val freezeTime: Instant?,
    val endTime: Instant
)
data class ContestProblemDto(
    val id: Int = 0,
    val contestId: Int = 0,
    val problemId: Int = 0,
    val ordinal: Int = 0
) : java.io.Serializable

data class ContestProblemsCacheWrapper(
    val problems: List<ContestProblemDto>
) : java.io.Serializable
data class ContestMemberDto(
    val id: Int = 0,
    val userId: Int = 0,
    val contestId: Int = 0,
    val resultPoints: Int? = null,
    val resultPlace: Int? = null
)

data class ContestProgressDto(
    val solvedCount: Int = 0,
    val totalUnsuccessful: Int = 0,
    val totalScore: Int = 0,
    val problemStats: Map<Int, ProblemStatDto> = emptyMap()
)

data class ProblemStatDto(
    val problemId: Int = 0,
    val isSolved: Boolean = false,
    val unsuccessfulCount: Int = 0,
    val score: Int = 0
)

data class ScoreboardDto(
    val contestId: Int = 0,
    val rows: List<ScoreboardRowDto> = emptyList()
) : java.io.Serializable

data class ScoreboardRowDto(
    val userId: Int = 0,
    val username: String = "",
    val solvedCount: Int = 0,
    val totalScore: Int = 0,
    val problemStats: Map<Int, ScoreboardProblemStatDto> = emptyMap(),
    val place: Int = 0
) : java.io.Serializable

data class ScoreboardProblemStatDto(
    val problemId: Int = 0,
    val isSolved: Boolean = false,
    val unsuccessfulCount: Int = 0,
    val frozenAttempts: Int = 0,
    val score: Int = 0
) : java.io.Serializable


data class CreateContestRequest(
    @field:Size(max = 1000)
    val name: String?,

    @field:Size(max = 99999)
    val overview: String?,

    @field:Size(max = 99999)
    val rules: String?,

    @field:Future
    val startTime: Instant,

    @field:Future
    val freezeTime: Instant?,

    @field:Future
    val endTime: Instant
)
data class AddContestProblemRequest(
    val problemId: Int,

    @field:Min(1)
    @field:Max(1000)
    val ordinal: Int
)

data class UpdateContestRequest(
    @field:Size(max = 1000)
    val name: String? = null,

    @field:Size(max = 99999)
    val overview: String? = null,

    @field:Size(max = 99999)
    val rules: String? = null,

    val startTime: Instant? = null,
    val freezeTime: Instant? = null,
    val endTime: Instant? = null
)

data class UpdateContestProblemsRequest(
    val problemIds: List<Int>
)
