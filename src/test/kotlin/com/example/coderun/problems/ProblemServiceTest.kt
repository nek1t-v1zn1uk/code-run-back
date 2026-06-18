package com.example.coderun.problems

import com.example.coderun.domain.problems.dto.CreateProblemRequest
import com.example.coderun.domain.problems.dto.UpdateProblemRequest
import com.example.coderun.domain.problems.entity.EvaluationType
import com.example.coderun.domain.problems.entity.Problem
import com.example.coderun.domain.problems.entity.ProblemDifficulty
import com.example.coderun.domain.problems.entity.ProblemTopic
import com.example.coderun.domain.problems.repository.ProblemRepository
import com.example.coderun.domain.problems.repository.ProblemTopicRepository
import com.example.coderun.domain.problems.service.ProblemService
import com.example.coderun.domain.tests.entity.ScriptChecker
import com.example.coderun.domain.tests.repository.ScriptCheckerRepository
import com.example.coderun.domain.solutions.entity.AvailableLanguage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class ProblemServiceTest {

    private val problemRepository: ProblemRepository = mockk()
    private val problemTopicRepository: ProblemTopicRepository = mockk()
    private val checkerRepository: ScriptCheckerRepository = mockk()

    private lateinit var problemService: ProblemService

    @BeforeEach
    fun setUp() {
        problemService = ProblemService(problemRepository, problemTopicRepository, checkerRepository)
    }

    @Test
    fun `createProblem should succeed when all references are valid`() {
        val topic = ProblemTopic(id = 1, name = "Dynamic Programming")
        val language = AvailableLanguage(id = 1, language = "python", version = "3.10")
        val checker = ScriptChecker(id = 2, name = "MyChecker", language = language, code = "checker-code")

        val request = CreateProblemRequest(
            title = "Fibonacci",
            topicId = 1,
            difficulty = ProblemDifficulty.EASY,
            statement = "Compute fibonacci",
            executionTimeLimitMs = 1000,
            executionMemoryLimitKb = 4096,
            defaultEvaluationType = EvaluationType.SCRIPT_CHECK,
            defaultScriptCheckerId = 2,
            isPublic = true
        )

        every { problemTopicRepository.findById(1) } returns Optional.of(topic)
        every { checkerRepository.findById(2) } returns Optional.of(checker)
        every { problemRepository.save(any()) } answers {
            val savedProblem = firstArg<Problem>()
            savedProblem.id = 42
            savedProblem
        }

        val result = problemService.createProblem(request)

        assertNotNull(result)
        assertEquals(42, result.id)
        assertEquals("Fibonacci", result.title)
        assertEquals(ProblemDifficulty.EASY, result.difficulty)
        assertEquals(2, result.defaultScriptCheckerId)

        verify { problemTopicRepository.findById(1) }
        verify { checkerRepository.findById(2) }
        verify { problemRepository.save(any()) }
    }

    @Test
    fun `createProblem should throw EntityNotFoundException when topic not found`() {
        val request = CreateProblemRequest(
            title = "Fibonacci",
            topicId = 99,
            difficulty = ProblemDifficulty.EASY,
            statement = "Compute fibonacci",
            executionTimeLimitMs = 1000,
            executionMemoryLimitKb = 4096,
            defaultEvaluationType = EvaluationType.EXACT_MATCH,
            isPublic = true
        )

        every { problemTopicRepository.findById(99) } returns Optional.empty()

        assertThrows<EntityNotFoundException> {
            problemService.createProblem(request)
        }

        verify { problemTopicRepository.findById(99) }
        verify(exactly = 0) { problemRepository.save(any()) }
    }

    @Test
    fun `getProblemDto should return DTO on success`() {
        val problem = Problem(
            id = 10,
            title = "Two Sum",
            difficulty = ProblemDifficulty.MEDIUM,
            statement = "Find two numbers",
            executionTimeLimitMs = 1500,
            executionMemoryLimitKb = 2048,
            defaultEvaluationType = EvaluationType.EXACT_MATCH
        )

        every { problemRepository.findById(10) } returns Optional.of(problem)

        val result = problemService.getProblemDto(10)

        assertEquals(10, result.id)
        assertEquals("Two Sum", result.title)

        verify { problemRepository.findById(10) }
    }

    @Test
    fun `getProblemDto should throw EntityNotFoundException when problem does not exist`() {
        every { problemRepository.findById(10) } returns Optional.empty()

        assertThrows<EntityNotFoundException> {
            problemService.getProblemDto(10)
        }
    }

    @Test
    fun `deleteProblem should call repository delete`() {
        every { problemRepository.deleteById(10) } returns Unit

        problemService.deleteProblem(10)

        verify { problemRepository.deleteById(10) }
    }
}
