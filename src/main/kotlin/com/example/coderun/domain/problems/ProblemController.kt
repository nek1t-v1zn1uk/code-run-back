package com.example.coderun.domain.problems

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/problems")
@Tag(name = "Problems")
class ProblemController (
    private val problemService: ProblemService,
) {
    @GetMapping("/topics")
    fun getProblemTopics(): ResponseEntity<*> {
        val problemTopics = problemService.getProblemTopics()
        return ResponseEntity.ok(problemTopics)
    }
    @GetMapping("/{id}")
    fun getProblemById(@PathVariable id: Int): ResponseEntity<*> {
        return try {
            val problemDto = problemService.getProblemDto(id)
            ResponseEntity.ok(problemDto)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to e.message))
        }
    }
    @GetMapping
    fun getProblems(@ModelAttribute request: GetProblemsRequest): ResponseEntity<*> {
        val problemsDto = problemService.getProblemWrapped(request)
        return ResponseEntity.ok(problemsDto)
    }
}