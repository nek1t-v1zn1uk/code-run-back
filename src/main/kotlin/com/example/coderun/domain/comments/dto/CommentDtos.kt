package com.example.coderun.domain.comments.dto

import java.time.Instant

data class CommentDto(
    val id: Int,
    val problemId: Int,
    val userId: Int,
    val userFirstName: String,
    val userLastName: String?,
    val userPhotoUrl: String?,
    val text: String,
    val pinnedSolutionId: Int?,
    val createdAt: Instant,
    val replies: List<CommentDto>
)

data class CreateCommentDto(
    val text: String,
    val parentId: Int? = null,
    val pinnedSolutionId: Int? = null
)
