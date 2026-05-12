package com.example.coderun.domain.tests.controller

import com.example.coderun.config.ValidatesInput
import com.example.coderun.domain.tests.dto.CreateScriptCheckerRequest
import com.example.coderun.domain.tests.dto.ScriptCheckerDto
import com.example.coderun.domain.tests.dto.UpdateScriptCheckerRequest
import com.example.coderun.domain.tests.service.ScriptCheckerService
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/script-checkers")
@Tag(name = "Problems")
class ScriptCheckerController(
    private val checkerService: ScriptCheckerService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ValidatesInput
    @ApiResponse(responseCode = "400", description = "Version of language must be specified")
    @ApiResponse(responseCode = "404", description = "Language/Language with version not found")
    fun createScriptChecker(
        @Valid @RequestBody request: CreateScriptCheckerRequest
    ): ResponseEntity<ScriptCheckerDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(checkerService.createScriptChecker(request))
    }

    @PatchMapping("/{checkerId}")
    @ResponseStatus(HttpStatus.OK)
    @ValidatesInput
    @ApiResponse(responseCode = "404", description = "Script checker/Language/Language with version not found")
    fun updateScriptChecker(
        @PathVariable checkerId: Int,
        @Valid @RequestBody request: UpdateScriptCheckerRequest
    ): ResponseEntity<ScriptCheckerDto> {
        return ResponseEntity.ok(checkerService.updateScriptChecker(checkerId, request))
    }

    @DeleteMapping("/{checkerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteScriptChecker(@PathVariable checkerId: Int): ResponseEntity<Void> {
        checkerService.deleteScriptChecker(checkerId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{checkerId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "Script checker not found")
    fun getScriptChecker(@PathVariable checkerId: Int): ResponseEntity<ScriptCheckerDto> {
        return ResponseEntity.ok(checkerService.getScriptCheckerDto(checkerId))
    }
}