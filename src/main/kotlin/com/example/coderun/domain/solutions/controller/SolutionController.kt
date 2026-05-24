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
@RequestMapping("/api/v1")
@Tag(name = "Solutions")
class SolutionController(
    private val solutionService: SolutionService
) {
    @PostMapping("problems/{problemId}/solutions")
    @ResponseStatus(HttpStatus.CREATED)
    @ValidatesInput
    @ApiResponse(responseCode = "400", description = "Version of language must be specified")
    @ApiResponse(responseCode = "404", description = "Problem/Language/Language with version/Contest/Contest problem not found")
    fun sendSolution(
        @PathVariable problemId: Int,
        @RequestBody @Valid request: SendSolutionRequest
    ): ResponseEntity<SolutionDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(solutionService.createSolution(problemId, request))
    }

    @GetMapping("/solutions/{solutionId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "Solution not found or authenticated user does not have permission")
    fun getSolution(@PathVariable solutionId: Int): ResponseEntity<SolutionDto> {
        return ResponseEntity.ok(solutionService.getSolutionDto(solutionId))
    }

    @GetMapping("problems/{problemId}/solutions")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "Problem not found")
    fun getSolutionsForProblem(@PathVariable problemId: Int): ResponseEntity<List<SolutionDto>> {
        return ResponseEntity.ok(solutionService.getSolutionsForProblem(problemId))
    }
}