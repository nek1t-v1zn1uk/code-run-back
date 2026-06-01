package com.example.coderun.domain.comments.controller

import com.example.coderun.domain.comments.dto.CommentDto
import com.example.coderun.domain.comments.dto.CreateCommentDto
import com.example.coderun.domain.comments.service.CommentService
import com.example.coderun.domain.users.User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/problems/{problemId}/comments")
class CommentController(
    private val commentService: CommentService
) {

    @GetMapping
    fun getComments(@PathVariable problemId: Int): ResponseEntity<List<CommentDto>> {
        return ResponseEntity.ok(commentService.getCommentsByProblem(problemId))
    }

    @PostMapping
    fun addComment(
        @PathVariable problemId: Int,
        @RequestBody dto: CreateCommentDto,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<CommentDto> {
        return ResponseEntity.ok(commentService.addComment(problemId, user.id!!, dto))
    }
}
