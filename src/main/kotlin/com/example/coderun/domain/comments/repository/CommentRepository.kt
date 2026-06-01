package com.example.coderun.domain.comments.repository

import com.example.coderun.domain.comments.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<Comment, Int> {
    fun findByProblemIdAndParentIsNullOrderByCreatedAtDesc(problemId: Int): List<Comment>
    fun existsByPinnedSolutionId(pinnedSolutionId: Int): Boolean
}
