package com.example.coderun.problems

import com.example.coderun.WebSliceTest
import com.example.coderun.domain.problems.controller.ProblemController
import com.example.coderun.domain.problems.dto.*
import com.example.coderun.domain.problems.entity.EvaluationType
import com.example.coderun.domain.problems.entity.ProblemDifficulty
import com.example.coderun.domain.problems.service.ProblemService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.stream.Stream

@WebMvcTest(ProblemController::class)
@AutoConfigureMockMvc(addFilters = false)
class ProblemControllerValidationTest : WebSliceTest() {

    @MockkBean
    private lateinit var problemService: ProblemService

    @Test
    fun `create problem should return 201 on success`() {
        val request = CreateProblemRequest(
            title = "Valid Problem Title",
            difficulty = ProblemDifficulty.EASY,
            statement = "This is a very long and detailed statement describing the problem setup.",
            executionTimeLimitMs = 1000,
            executionMemoryLimitKb = 4096,
            defaultEvaluationType = EvaluationType.EXACT_MATCH,
            isPublic = true
        )

        val responseDto = ProblemDto(
            id = 1,
            title = request.title,
            difficulty = request.difficulty!!,
            statement = request.statement,
            executionTimeLimitMs = request.executionTimeLimitMs,
            executionMemoryLimitKb = request.executionMemoryLimitKb,
            defaultEvaluationType = request.defaultEvaluationType,
            createdAt = Instant.now(),
            isPublic = request.isPublic
        )

        every { problemService.createProblem(any()) } returns responseDto

        mockMvc.perform(post("/api/v1/problems")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Valid Problem Title"))

        verify { problemService.createProblem(any()) }
    }

    @ParameterizedTest(name = "Create Problem should fail when {1} is invalid")
    @MethodSource("invalidCreateProblemProvider")
    fun `create problem should return 400 for invalid inputs`(
        invalidPayload: Map<String, Any?>,
        expectedErrorField: String
    ) {
        mockMvc.perform(post("/api/v1/problems")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidPayload)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.$expectedErrorField").exists())
    }

    @Test
    fun `get problem by id should return problem info`() {
        val problemDto = ProblemDto(
            id = 42,
            title = "A Problem",
            difficulty = ProblemDifficulty.MEDIUM,
            statement = "Statement details",
            executionTimeLimitMs = 1000,
            executionMemoryLimitKb = 2048,
            defaultEvaluationType = EvaluationType.SCRIPT_CHECK,
            createdAt = Instant.now()
        )

        every { problemService.getProblemDto(42) } returns problemDto

        mockMvc.perform(get("/api/v1/problems/42"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.title").value("A Problem"))
    }

    @Test
    fun `get problem by id should return 404 when not found`() {
        every { problemService.getProblemDto(999) } throws EntityNotFoundException("Problem not found")

        mockMvc.perform(get("/api/v1/problems/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Problem not found"))
    }

    @Test
    fun `delete problem should return 204`() {
        every { problemService.deleteProblem(10) } returns Unit

        mockMvc.perform(delete("/api/v1/problems/10"))
            .andExpect(status().isNoContent)

        verify { problemService.deleteProblem(10) }
    }

    companion object {
        @JvmStatic
        fun invalidCreateProblemProvider(): Stream<Arguments> = Stream.of(
            // Title checks
            arguments(mapOf(
                "title" to "",
                "difficulty" to "EASY", "statement" to "A long enough statement...",
                "execution_time_limit_ms" to 1000, "execution_memory_limit_kb" to 4096,
                "default_evaluation_type" to "EXACT_MATCH"
            ), "title"),
            arguments(mapOf(
                "title" to "ab",
                "difficulty" to "EASY", "statement" to "A long enough statement...",
                "execution_time_limit_ms" to 1000, "execution_memory_limit_kb" to 4096,
                "default_evaluation_type" to "EXACT_MATCH"
            ), "title"),
            arguments(mapOf(
                "title" to "a".repeat(256),
                "difficulty" to "EASY", "statement" to "A long enough statement...",
                "execution_time_limit_ms" to 1000, "execution_memory_limit_kb" to 4096,
                "default_evaluation_type" to "EXACT_MATCH"
            ), "title"),
            // Statement checks
            arguments(mapOf(
                "title" to "Valid Title",
                "difficulty" to "EASY", "statement" to "short",
                "execution_time_limit_ms" to 1000, "execution_memory_limit_kb" to 4096,
                "default_evaluation_type" to "EXACT_MATCH"
            ), "statement"),
            // Time limit checks
            arguments(mapOf(
                "title" to "Valid Title",
                "difficulty" to "EASY", "statement" to "A long enough statement...",
                "execution_time_limit_ms" to 499, "execution_memory_limit_kb" to 4096,
                "default_evaluation_type" to "EXACT_MATCH"
            ), "execution_time_limit_ms"),
            arguments(mapOf(
                "title" to "Valid Title",
                "difficulty" to "EASY", "statement" to "A long enough statement...",
                "execution_time_limit_ms" to 10001, "execution_memory_limit_kb" to 4096,
                "default_evaluation_type" to "EXACT_MATCH"
            ), "execution_time_limit_ms"),
            // Memory limit checks
            arguments(mapOf(
                "title" to "Valid Title",
                "difficulty" to "EASY", "statement" to "A long enough statement...",
                "execution_time_limit_ms" to 1000, "execution_memory_limit_kb" to 511,
                "default_evaluation_type" to "EXACT_MATCH"
            ), "execution_memory_limit_kb"),
            arguments(mapOf(
                "title" to "Valid Title",
                "difficulty" to "EASY", "statement" to "A long enough statement...",
                "execution_time_limit_ms" to 1000, "execution_memory_limit_kb" to 524289,
                "default_evaluation_type" to "EXACT_MATCH"
            ), "execution_memory_limit_kb")
        )
    }
}
