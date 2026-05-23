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
    val id: Int,
    val contestId: Int,
    val problemId: Int,
    val ordinal: Int
)
data class ContestMemberDto(
    val id: Int,
    val userId: Int,
    val contestId: Int,
    val resultPoints: Int?,
    val resultPlace: Int?
)

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
