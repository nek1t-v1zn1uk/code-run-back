package com.example.coderun.domain.tests.controller

import com.example.coderun.config.ValidatesInput
import com.example.coderun.domain.tests.dto.UpdateTestListRequest
import com.example.coderun.domain.tests.service.TestService
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/problems/{problemId}/tests")
@Tag(name = "Problems")
class TestsController(
    private val testService: TestService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ValidatesInput
    @ApiResponse(responseCode = "404", description = "Problem not found")
    fun updateTestList(
        @PathVariable problemId: Int,
        @RequestBody @Valid request: UpdateTestListRequest
    ): ResponseEntity<Unit> {
        testService.updateTestList(problemId, request)
        return ResponseEntity.noContent().build()
    }
}