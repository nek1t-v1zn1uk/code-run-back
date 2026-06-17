package com.example.coderun.domain.codeexecution

import com.example.coderun.domain.isolaterunner.IsolateService
import org.springframework.stereotype.Service
import java.time.Instant.now

@Service
class CodeExecutionService(
    private val isolateService: IsolateService
) {
    fun runCode(request: CodeExecutionRequest): CodeExecutionResponse {
        return try {
            val codeResult = isolateService.executeCode(request.code, request.language, request.input)
            CodeExecutionResponse(
                status = when(codeResult.status) {
                    "OK" -> "OK"
                    "TO" -> "Time Limit Exceeded"
                    "RE" -> "Runtime Error"
                    "SG" -> "Memory Limit Exceeded"
                    "XX" -> "Internal Error of the Sandbox"
                    else -> "Unknown Error"
                },
                exitCode = codeResult.exitCode,
                output = codeResult.stdout,
                time = codeResult.time,
                memory = codeResult.memory,
                error = codeResult.stderr
            )
        } catch (e: Exception) {
            val rawError = e.message ?: "Unknown compilation error"
            val sanitizedError = rawError
                .replace(Regex("Isolate error: Compilation failed: exit status \\d+ \\| Stderr: "), "")
                .replace(Regex("/tmp/isolate_compile_\\d+/[a-zA-Z0-9_.-]+:"), "line ")
                .trim()
                
            CodeExecutionResponse(
                status = "Compilation Error",
                exitCode = 1,
                output = "",
                time = 0.0,
                memory = 0,
                error = sanitizedError
            )
        }
    }
}