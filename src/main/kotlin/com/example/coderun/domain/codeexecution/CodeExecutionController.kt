package com.example.coderun.domain.codeexecution

import com.example.coderun.domain.isolaterunner.IsolateCodeResult
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/code-execution")
@Tag(name = "Code Execution")
class CodeExecutionController(
    private val codeExecutionService: CodeExecutionService
) {
    @PostMapping
    fun runCode(@RequestBody request: CodeExecutionRequest): ResponseEntity<*> {
        val response = codeExecutionService.runCode(request)
        return ResponseEntity.ok(response)
    }
}