package com.example.coderun.domain.contest.controller

import com.example.coderun.config.AdminOnly
import com.example.coderun.config.ValidatesInput
import com.example.coderun.domain.contest.dto.*
import com.example.coderun.domain.contest.service.ContestService
import com.example.coderun.domain.users.User
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/contests")
class ContestController(
    private val contestService: ContestService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllContests(): ResponseEntity<List<ContestDto>> {
        return ResponseEntity.ok(contestService.getAllContests())
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "Contest not found")
    fun getContestById(@PathVariable id: Int): ResponseEntity<ContestDto> {
        return ResponseEntity.ok(contestService.getContestById(id))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @AdminOnly
    @ValidatesInput
    fun createContest(@Valid @RequestBody request: CreateContestRequest): ResponseEntity<ContestDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(contestService.createContest(request))
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @AdminOnly
    @ValidatesInput
    fun updateContest(
        @PathVariable id: Int,
        @Valid @RequestBody request: UpdateContestRequest
    ): ResponseEntity<ContestDto> {
        return ResponseEntity.ok(contestService.updateContest(id, request))
    }

    @GetMapping("/{id}/problems")
    @ResponseStatus(HttpStatus.OK)
    fun getContestProblems(@PathVariable id: Int): ResponseEntity<List<ContestProblemDto>> {
        return ResponseEntity.ok(contestService.getContestProblems(id))
    }

    @PostMapping("/{id}/problems")
    @ResponseStatus(HttpStatus.CREATED)
    @AdminOnly
    @ValidatesInput
    @ApiResponse(responseCode = "404", description = "Contest/Problem not found")
    fun addProblemToContest(
        @PathVariable id: Int,
        @Valid @RequestBody request: AddContestProblemRequest
    ): ResponseEntity<ContestProblemDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(contestService.addProblemToContest(id, request))
    }

    @PutMapping("/{id}/problems")
    @ResponseStatus(HttpStatus.OK)
    @AdminOnly
    @ValidatesInput
    fun updateContestProblems(
        @PathVariable id: Int,
        @Valid @RequestBody request: UpdateContestProblemsRequest
    ): ResponseEntity<List<ContestProblemDto>> {
        return ResponseEntity.ok(contestService.updateContestProblems(id, request))
    }

    @GetMapping("/{id}/members")
    @ResponseStatus(HttpStatus.OK)
    fun getContestMembers(@PathVariable id: Int): ResponseEntity<List<ContestMemberDto>> {
        return ResponseEntity.ok(contestService.getContestMembers(id))
    }

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "400", description = "User already joined")
    @ApiResponse(responseCode = "404", description = "Contest/User not found")
    fun joinContest(@PathVariable id: Int): ResponseEntity<ContestMemberDto> {
        val authentication = SecurityContextHolder.getContext().authentication!!
        val user = authentication.principal as User
        return ResponseEntity.ok(contestService.joinContest(id, user.id!!))
    }
}
