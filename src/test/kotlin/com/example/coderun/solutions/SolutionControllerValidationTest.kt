package com.example.coderun.solutions

import com.example.coderun.WebSliceTest
import com.example.coderun.domain.solutions.controller.SolutionController
import com.example.coderun.domain.solutions.dto.SendSolutionRequest
import com.example.coderun.domain.solutions.dto.SolutionDto
import com.example.coderun.domain.solutions.entity.AvailableLanguage
import com.example.coderun.domain.solutions.entity.SolutionStatus
import com.example.coderun.domain.solutions.service.SolutionService
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

@WebMvcTest(SolutionController::class)
@AutoConfigureMockMvc(addFilters = false)
class SolutionControllerValidationTest : WebSliceTest() {

    @MockkBean
    private lateinit var solutionService: SolutionService

    @Test
    fun `send solution should return 201 on success`() {
        val request = SendSolutionRequest(
            code = "print('hello')",
            language = "python",
            languageVersion = "3.10"
        )
        val expectedDto = SolutionDto(
            id = 1,
            problemId = 10,
            userId = 2,
            code = request.code,
            language = AvailableLanguage(id = 1, language = "python", version = "3.10"),
            status = SolutionStatus.IN_QUEUE,
            sentAt = Instant.now()
        )

        every { solutionService.createSolution(10, any()) } returns expectedDto

        mockMvc.perform(post("/api/v1/problems/10/solutions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.code").value("print('hello')"))
            .andExpect(jsonPath("$.language.language").value("python"))

        verify { solutionService.createSolution(10, any()) }
    }

    @ParameterizedTest(name = "Send Solution should fail when {1} is invalid")
    @MethodSource("invalidSendSolutionProvider")
    fun `send solution should return 400 for invalid inputs`(
        invalidPayload: Map<String, Any?>,
        expectedErrorField: String
    ) {
        mockMvc.perform(post("/api/v1/problems/10/solutions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidPayload)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.$expectedErrorField").exists())
    }

    @Test
    fun `get solution should return solution info`() {
        val expectedDto = SolutionDto(
            id = 5,
            problemId = 12,
            userId = 2,
            code = "some-code",
            language = AvailableLanguage(id = 2, language = "cpp", version = "20"),
            status = SolutionStatus.SUCCESS,
            sentAt = Instant.now()
        )

        every { solutionService.getSolutionDto(5) } returns expectedDto

        mockMvc.perform(get("/api/v1/solutions/5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.status").value("SUCCESS"))
    }

    @Test
    fun `get solution should return 404 when not found`() {
        every { solutionService.getSolutionDto(99) } throws EntityNotFoundException("Solution not found")

        mockMvc.perform(get("/api/v1/solutions/99"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Solution not found"))
    }

    companion object {
        @JvmStatic
        fun invalidSendSolutionProvider(): Stream<Arguments> = Stream.of(
            // Blank code
            arguments(mapOf(
                "code" to "",
                "language" to "python",
                "language_version" to "3.10"
            ), "code"),
            // Blank language
            arguments(mapOf(
                "code" to "print()",
                "language" to "",
                "language_version" to "3.10"
            ), "language"),
            // Code too long
            arguments(mapOf(
                "code" to "a".repeat(50001),
                "language" to "python",
                "language_version" to "3.10"
            ), "code"),
            // Language too long
            arguments(mapOf(
                "code" to "print()",
                "language" to "python_super_long_name_version_check",
                "language_version" to "3.10"
            ), "language")
        )
    }
}
