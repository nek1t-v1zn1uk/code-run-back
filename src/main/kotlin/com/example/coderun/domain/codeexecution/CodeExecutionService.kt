package com.example.coderun.domain.codeexecution

import com.example.coderun.domain.isolaterunner.IsolateService
import org.springframework.stereotype.Service
import java.time.Instant.now

@Service
class CodeExecutionService(
    private val isolateService: IsolateService
) {
    fun runCode(request: CodeExecutionRequest): CodeExecutionResponse {
        val codeResult = isolateService.executeCode(request.code, request.input)
        return CodeExecutionResponse(
            status = when(codeResult.status) {
                "OK" -> "OK"
                "TO" -> "Timed Out"
                "RE" -> "Run-Time Error, i.e., exited with a non-zero exit code"
                "XX" -> "Internal Error of the Sandbox"
                else -> "Unknown Error"
            },
            exitCode = codeResult.exitCode,
            output = codeResult.stdout,
            time = codeResult.time,
            memory = codeResult.memory,
            error = codeResult.stderr
        )
    }
}