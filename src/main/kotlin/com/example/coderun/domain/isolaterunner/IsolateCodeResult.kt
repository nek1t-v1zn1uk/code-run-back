package com.example.coderun.domain.isolaterunner

data class IsolateCodeResult(
    val status: String, // OK, TO, RE ...
    val exitCode: Int,
    val time: Double,
    val memory: Long,
    val stdout: String,
    val stderr: String,
)