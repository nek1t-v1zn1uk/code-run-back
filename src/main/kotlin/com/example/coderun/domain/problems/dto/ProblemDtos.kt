package com.example.coderun.domain.problems.dto

import com.example.coderun.domain.problems.entity.EvaluationType
import com.example.coderun.domain.problems.entity.ProblemDifficulty
import com.example.coderun.domain.problems.entity.ProblemTopic
import java.time.Instant

data class ProblemDto (
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

data class GetProblemsRequest (
    val difficulty: ProblemDifficulty?,
    val topicName: String?,
    val limit: Int = 10,
    val cursor: String?,
)
data class ProblemPageResponse (
    val content: List<ProblemDto>,
    val hasNext: Boolean,
    val nextCursor: String?,
    val isEmpty: Boolean = content.isEmpty()
)
data class ProblemCursor (
    val lastSeenId: Int,
    val lastSeenDifficulty: Int // Int for the Ordinal value
)
