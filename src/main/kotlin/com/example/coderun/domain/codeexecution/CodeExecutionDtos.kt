package com.example.coderun.domain.codeexecution

data class CodeExecutionRequest(
    val code: String,
    val input: String,
)
data class CodeExecutionResponse(
    val status: String,
    val exitCode: Int,
    val output: String,
    val time: Double,
    val memory: Long,
    val error: String,
)