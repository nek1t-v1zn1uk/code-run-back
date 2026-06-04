package com.example.coderun.domain.solutions.service

import com.example.coderun.domain.comments.repository.CommentRepository
import com.example.coderun.domain.problems.repository.ProblemRepository
import com.example.coderun.domain.solutions.dto.SendSolutionRequest
import com.example.coderun.domain.solutions.dto.SolutionDto
import com.example.coderun.domain.solutions.entity.Solution
import com.example.coderun.domain.solutions.repository.AvailableLanguageRepository
import com.example.coderun.domain.solutions.repository.SolutionRepository
import com.example.coderun.domain.users.User
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

import com.example.coderun.domain.contest.repository.ContestRepository
import com.example.coderun.domain.contest.repository.ContestProblemRepository
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.amqp.rabbit.core.RabbitTemplate

@Service
class SolutionService(
    private val solutionRepository: SolutionRepository,
    private val problemRepository: ProblemRepository,
    private val languageRepository: AvailableLanguageRepository,
    private val evaluationService: SolutionEvaluationService,
    private val contestRepository: ContestRepository,
    private val contestProblemRepository: ContestProblemRepository,
    private val commentRepository: CommentRepository,
    private val broadcastService: SolutionBroadcastService,
    private val rabbitTemplate: RabbitTemplate,
) {
    @Transactional
    fun createSolution(problemId: Int, request: SendSolutionRequest): SolutionDto {
        val problem = problemRepository.findById(problemId).getOrNull()
            ?: throw EntityNotFoundException("Problem with id '$problemId' not found")

        val language =
            when(languageRepository.countByLanguage(request.language)) {
                0 -> throw EntityNotFoundException("Language '${request.language}' not found")
                1 -> languageRepository.findByLanguage(request.language)!!
                else ->
                    request.languageVersion?.let{
                        languageRepository.findByLanguageAndVersion(request.language, request.languageVersion)
                            ?: throw EntityNotFoundException("Language '${request.language}' with version '${request.languageVersion}' not found")
                    } ?: throw IllegalArgumentException("Language '${request.language}' must have specified version")
            }

        val authentication = SecurityContextHolder.getContext().authentication!!
        val user = authentication.principal as User

        val contest = request.contestId?.let { 
            contestRepository.findById(it).getOrNull()
                ?: throw EntityNotFoundException("Contest with id '$it' not found")
        }

        val contestProblem = request.contestProblemId?.let {
            contestProblemRepository.findById(it).getOrNull()
                ?: throw EntityNotFoundException("Contest problem with id '$it' not found")
        }

        val newSolution = solutionRepository.save(
            Solution(
                problem = problem,
                user = user,
                code = request.code,
                language = language,
                contest = contest,
                contestProblem = contestProblem
            )
        )

        broadcastService.broadcastSolutionUpdate(newSolution)
        
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                rabbitTemplate.convertAndSend("solution-queue", newSolution.id.toString())
            }
        })

        return newSolution.toDto()
    }

    fun getSolutionDto(solutionId: Int): SolutionDto {
        val authentication = SecurityContextHolder.getContext().authentication!!
        val user = authentication.principal as User

        val solution = solutionRepository.findById(solutionId).getOrNull()
            ?: throw EntityNotFoundException("Solution with id '$solutionId' not found")

        if (solution.user.id != user.id && !commentRepository.existsByPinnedSolutionId(solutionId))
            throw EntityNotFoundException("Solution with id '$solutionId' not found")

        return solution.toDto()
    }

    fun getSolutionsForProblem(problemId: Int): List<SolutionDto> {
        val authentication = SecurityContextHolder.getContext().authentication!!
        val user = authentication.principal as User

        val solutions = solutionRepository.findAllByProblemIdAndUserIdOrderBySentAtDesc(problemId, user.id!!)
        return solutions.map { it.toDto() }
    }
}