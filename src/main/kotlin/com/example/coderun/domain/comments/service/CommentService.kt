package com.example.coderun.domain.comments.service

import com.example.coderun.domain.comments.dto.CommentDto
import com.example.coderun.domain.comments.dto.CreateCommentDto
import com.example.coderun.domain.comments.entity.Comment
import com.example.coderun.domain.comments.repository.CommentRepository
import com.example.coderun.domain.problems.repository.ProblemRepository
import com.example.coderun.domain.solutions.repository.SolutionRepository
import com.example.coderun.domain.users.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
    private val solutionRepository: SolutionRepository
) {

    @Transactional(readOnly = true)
    fun getCommentsByProblem(problemId: Int): List<CommentDto> {
        val topLevelComments = commentRepository.findByProblemIdAndParentIsNullOrderByCreatedAtDesc(problemId)
        return topLevelComments.map { toDto(it) }
    }

    @Transactional
    fun addComment(problemId: Int, userId: Int, dto: CreateCommentDto): CommentDto {
        val problem = problemRepository.findById(problemId).orElseThrow { IllegalArgumentException("Problem not found") }
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        
        val parent = dto.parentId?.let { 
            commentRepository.findById(it).orElseThrow { IllegalArgumentException("Parent comment not found") }
        }

        val pinnedSolution = dto.pinnedSolutionId?.let {
            solutionRepository.findById(it).orElseThrow { IllegalArgumentException("Solution not found") }
        }

        if (pinnedSolution != null && pinnedSolution.user.id != user.id) {
            throw IllegalArgumentException("Cannot pin someone else's solution")
        }
        
        if (pinnedSolution != null && pinnedSolution.problem.id != problemId) {
            throw IllegalArgumentException("Pinned solution must belong to the same problem")
        }

        val comment = Comment(
            problem = problem,
            user = user,
            text = dto.text,
            parent = parent,
            pinnedSolution = pinnedSolution
        )

        return toDto(commentRepository.save(comment))
    }

    private fun toDto(comment: Comment): CommentDto {
        return CommentDto(
            id = comment.id!!,
            problemId = comment.problem.id!!,
            userId = comment.user.id!!,
            userFirstName = comment.user.firstName,
            userLastName = comment.user.lastName,
            userPhotoUrl = comment.user.photoUrl,
            text = comment.text,
            pinnedSolutionId = comment.pinnedSolution?.id,
            createdAt = comment.createdAt,
            replies = comment.replies.sortedBy { it.createdAt }.map { toDto(it) }
        )
    }
}
