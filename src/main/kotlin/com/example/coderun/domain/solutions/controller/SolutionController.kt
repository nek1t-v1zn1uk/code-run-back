package com.example.coderun.domain.solutions.controller

import com.example.coderun.config.ValidatesInput
import com.example.coderun.domain.solutions.dto.SendSolutionRequest
import com.example.coderun.domain.solutions.dto.SolutionDto
import com.example.coderun.domain.solutions.service.SolutionService
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/problems/{problemId}/solutions")
@Tag(name = "Solutions")
class SolutionController(
    private val solutionService: SolutionService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ValidatesInput
    @ApiResponse(responseCode = "400", description = "Version of language must be specified")
    @ApiResponse(responseCode = "404", description = "Problem/Language/Language with version not found")
    fun sendSolution(
        @PathVariable problemId: Int,
        @RequestBody @Valid request: SendSolutionRequest
    ): ResponseEntity<SolutionDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(solutionService.createSolution(problemId, request))
    }
}