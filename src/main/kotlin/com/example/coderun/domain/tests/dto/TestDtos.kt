package com.example.coderun.domain.tests.dto

import com.example.coderun.domain.problems.entity.EvaluationType
import jakarta.validation.constraints.NotNull

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
    var overrideEvaluationType: EvaluationType? = null,
)
data class UpdateTestRequest(
    @field:NotNull
    val id: Int? = null,
    val ordinal: Int? = null,
    val isExample: Boolean? = null,
    val inputData: String? = null,
    val expectedOutput: String? = null,
    var overrideEvaluationType: EvaluationType? = null,
)
data class DeleteTestRequest(
    @field:NotNull
    val id: Int? = null
)