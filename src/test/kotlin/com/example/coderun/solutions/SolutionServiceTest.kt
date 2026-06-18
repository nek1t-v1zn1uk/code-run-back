package com.example.coderun.solutions

import com.example.coderun.domain.comments.repository.CommentRepository
import com.example.coderun.domain.contest.repository.ContestProblemRepository
import com.example.coderun.domain.contest.repository.ContestRepository
import com.example.coderun.domain.problems.entity.Problem
import com.example.coderun.domain.problems.entity.ProblemDifficulty
import com.example.coderun.domain.problems.repository.ProblemRepository
import com.example.coderun.domain.solutions.dto.SendSolutionRequest
import com.example.coderun.domain.solutions.entity.AvailableLanguage
import com.example.coderun.domain.solutions.entity.Solution
import com.example.coderun.domain.solutions.entity.SolutionStatus
import com.example.coderun.domain.solutions.repository.AvailableLanguageRepository
import com.example.coderun.domain.solutions.repository.SolutionRepository
import com.example.coderun.domain.solutions.service.SolutionBroadcastService
import com.example.coderun.domain.solutions.service.SolutionEvaluationService
import com.example.coderun.domain.solutions.service.SolutionService
import com.example.coderun.domain.users.User
import com.example.coderun.domain.users.UserRoles
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.*

class SolutionServiceTest {

    private val solutionRepository: SolutionRepository = mockk()
    private val problemRepository: ProblemRepository = mockk()
    private val languageRepository: AvailableLanguageRepository = mockk()
    private val evaluationService: SolutionEvaluationService = mockk()
    private val contestRepository: ContestRepository = mockk()
    private val contestProblemRepository: ContestProblemRepository = mockk()
    private val commentRepository: CommentRepository = mockk()
    private val broadcastService: SolutionBroadcastService = mockk()
    private val rabbitTemplate: RabbitTemplate = mockk()

    private lateinit var solutionService: SolutionService
    private lateinit var mockUser: User

    @BeforeEach
    fun setUp() {
        solutionService = SolutionService(
            solutionRepository,
            problemRepository,
            languageRepository,
            evaluationService,
            contestRepository,
            contestProblemRepository,
            commentRepository,
            broadcastService,
            rabbitTemplate
        )
        mockUser = User(id = 1, email = "sol@example.com", passwordHash = "hash", firstName = "Dev", role = UserRoles.USER)
        val authentication = UsernamePasswordAuthenticationToken(mockUser, null, mockUser.authorities)
        SecurityContextHolder.getContext().authentication = authentication

        TransactionSynchronizationManager.initSynchronization()
    }

    @AfterEach
    fun tearDown() {
        TransactionSynchronizationManager.clear()
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `createSolution should succeed when language and problem are valid`() {
        val problem = Problem(id = 10, title = "P", difficulty = ProblemDifficulty.EASY, statement = "S", executionTimeLimitMs = 1000, executionMemoryLimitKb = 1024)
        val language = AvailableLanguage(id = 1, language = "python", version = "3.10")
        val request = SendSolutionRequest(code = "print()", language = "python", languageVersion = "3.10")

        every { problemRepository.findById(10) } returns Optional.of(problem)
        every { languageRepository.countByLanguage("python") } returns 2
        every { languageRepository.findByLanguageAndVersion("python", "3.10") } returns language
        every { solutionRepository.save(any()) } answers {
            val sol = firstArg<Solution>()
            sol.id = 55
            sol
        }
        every { broadcastService.broadcastSolutionUpdate(any()) } returns Unit

        val result = solutionService.createSolution(10, request)

        assertNotNull(result)
        assertEquals(55, result.id)
        assertEquals(SolutionStatus.IN_QUEUE, result.status)

        verify { problemRepository.findById(10) }
        verify { languageRepository.findByLanguageAndVersion("python", "3.10") }
        verify { solutionRepository.save(any()) }
        verify { broadcastService.broadcastSolutionUpdate(any()) }
    }

    @Test
    fun `createSolution should throw EntityNotFoundException when problem not found`() {
        val request = SendSolutionRequest(code = "print()", language = "python")
        every { problemRepository.findById(99) } returns Optional.empty()

        assertThrows<EntityNotFoundException> {
            solutionService.createSolution(99, request)
        }

        verify { problemRepository.findById(99) }
        verify(exactly = 0) { solutionRepository.save(any()) }
    }
}
