package com.example.coderun.domain.problems.controller

import com.example.coderun.domain.problems.dto.CreateProblemTopicRequest
import com.example.coderun.domain.problems.dto.UpdateProblemTopicRequest
import com.example.coderun.domain.problems.entity.ProblemTopic
import com.example.coderun.domain.problems.service.ProblemTopicService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/topics")
@Tag(name = "Admin Topics", description = "Admin API for managing problem topics")
@PreAuthorize("hasRole('ADMIN')")
class ProblemTopicAdminController(
    private val problemTopicService: ProblemTopicService
) {

    @PostMapping
    fun createTopic(@Valid @RequestBody request: CreateProblemTopicRequest): ResponseEntity<ProblemTopic> {
        val topic = problemTopicService.createTopic(request)
        return ResponseEntity(topic, HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateTopic(
        @PathVariable id: Int,
        @Valid @RequestBody request: UpdateProblemTopicRequest
    ): ResponseEntity<ProblemTopic> {
        val topic = problemTopicService.updateTopic(id, request)
        return ResponseEntity.ok(topic)
    }

    @DeleteMapping("/{id}")
    fun deleteTopic(@PathVariable id: Int): ResponseEntity<Void> {
        problemTopicService.deleteTopic(id)
        return ResponseEntity.noContent().build()
    }
}
