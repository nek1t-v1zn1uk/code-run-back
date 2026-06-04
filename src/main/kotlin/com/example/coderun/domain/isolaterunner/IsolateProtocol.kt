package com.example.coderun.domain.isolaterunner

data class IsolateRequest(
    val type: String,
    val code: String? = null,
    val language: String? = null,
    val binaryPath: String? = null,
    val input: String? = null,
    val timeLimitSec: Float? = null,
    val memoryLimitKb: Int? = null
)

data class IsolateResponse(
    val success: Boolean,
    val binaryPath: String? = null,
    val result: IsolateCodeResult? = null,
    val error: String? = null
)
