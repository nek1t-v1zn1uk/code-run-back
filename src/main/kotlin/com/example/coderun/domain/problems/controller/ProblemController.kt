package com.example.coderun.domain.problems.controller

import com.example.coderun.config.AdminOnly
import com.example.coderun.config.ValidatesInput
import com.example.coderun.domain.problems.dto.CreateProblemRequest
import com.example.coderun.domain.problems.dto.GetProblemsRequest
import com.example.coderun.domain.problems.dto.ProblemDto
import com.example.coderun.domain.problems.dto.ProblemPageResponse
import com.example.coderun.domain.problems.dto.UpdateProblemRequest
import com.example.coderun.domain.problems.entity.ProblemTopic
import com.example.coderun.domain.problems.service.ProblemService
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/problems")
@Tag(name = "Problems")
class ProblemController (
    private val problemService: ProblemService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @AdminOnly
    @ValidatesInput
    @ApiResponse(responseCode = "404", description = "Problem topic/Script Checker not found")
    fun createProblem(@Valid @RequestBody request: CreateProblemRequest): ResponseEntity<ProblemDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(problemService.createProblem(request))
    }
    @PatchMapping("/{problemId}")
    @ResponseStatus(HttpStatus.OK)
    @AdminOnly
    @ValidatesInput
    @ApiResponse(responseCode = "404", description = "Problem/Script Checker not found")
    fun updateProblem(@PathVariable problemId: Int, @Valid @RequestBody request: UpdateProblemRequest): ResponseEntity<ProblemDto> {
        return ResponseEntity.ok(problemService.updateProblem(problemId, request))
    }
    @DeleteMapping("/{problemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AdminOnly
    @ApiResponse(responseCode = "404", description = "Problem not found")
    fun deleteProblem(@PathVariable problemId: Int): ResponseEntity<Unit> {
        problemService.deleteProblem(problemId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/topics")
    @ResponseStatus(HttpStatus.OK)
    fun getProblemTopics(): ResponseEntity<List<ProblemTopic>> {
        val problemTopics = problemService.getProblemTopics()
        return ResponseEntity.ok(problemTopics)
    }
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "Problem not found")
    fun getProblemById(@PathVariable id: Int): ResponseEntity<ProblemDto> {
        return ResponseEntity.ok(problemService.getProblemDto(id))
    }
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ValidatesInput
    fun getProblems(@Valid @ModelAttribute request: GetProblemsRequest): ResponseEntity<ProblemPageResponse> {
        return ResponseEntity.ok(problemService.getProblemsWrapped(request))
    }
}