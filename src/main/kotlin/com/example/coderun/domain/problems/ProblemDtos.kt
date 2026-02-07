package com.example.coderun.domain.problems

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