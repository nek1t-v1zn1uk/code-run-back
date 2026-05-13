package com.example.coderun.domain.tests.dto

import com.example.coderun.domain.problems.entity.EvaluationType
import com.example.coderun.domain.problems.entity.Problem
import com.example.coderun.domain.tests.entity.ScriptChecker
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

data class TestDto(
    var id: Int,
    var problemId: Int,
    var ordinal: Int,
    var isExample: Boolean,
    var inputData: String,
    var expectedOutput: String? = null,
    var overrideEvaluationType: EvaluationType? = null,
    var overrideScriptCheckerId: Int? = null,
    var createdAt: Instant
)

data class UpdateTestListRequest(
    val newTests: List<NewTestRequest> = emptyList(),
    val updateTests: List<UpdateTestRequest> = emptyList(),
    val deleteTests: List<DeleteTestRequest> = emptyList(),
)
data class NewTestRequest(
    @field:NotNull
    val ordinal: Int? = null,
    @field:NotNull
    val isExample: Boolean? = null,
    @field:NotNull
    val inputData: String? = null,
    val expectedOutput: String? = null,
    val overrideEvaluationType: EvaluationType? = null,
    val overrideScriptCheckerId: Int? = null,
)
data class UpdateTestRequest(
    @field:NotNull
    val id: Int? = null,
    val ordinal: Int? = null,
    val isExample: Boolean? = null,
    val inputData: String? = null,
    val expectedOutput: String? = null,
    var overrideEvaluationType: EvaluationType? = null,
    val overrideScriptCheckerId: Int? = null,
)
data class DeleteTestRequest(
    @field:NotNull
    val id: Int? = null
)