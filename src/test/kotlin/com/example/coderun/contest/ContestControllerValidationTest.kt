package com.example.coderun.contest

import com.example.coderun.WebSliceTest
import com.example.coderun.domain.contest.controller.ContestController
import com.example.coderun.domain.contest.dto.*
import com.example.coderun.domain.contest.service.ContestService
import com.example.coderun.domain.users.User
import com.example.coderun.domain.users.UserRoles
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.stream.Stream

@WebMvcTest(ContestController::class)
@AutoConfigureMockMvc(addFilters = false)
class ContestControllerValidationTest : WebSliceTest() {

    @MockkBean
    private lateinit var contestService: ContestService

    private lateinit var mockUser: User

    @BeforeEach
    fun setupSecurityContext() {
        mockUser = User(
            email = "contest-user@example.com",
            passwordHash = "hash",
            firstName = "Alice",
            lastName = "Bob",
            role = UserRoles.USER
        )
        mockUser.id = 1337
        val authentication = UsernamePasswordAuthenticationToken(mockUser, null, mockUser.authorities)
        SecurityContextHolder.getContext().authentication = authentication
    }

    @Test
    fun `get all contests should return success list`() {
        val list = listOf(
            ContestDto(1, "Contest A", "Overview A", "Rules A", Instant.now(), null, Instant.now().plusSeconds(3600))
        )
        every { contestService.getAllContests() } returns list

        mockMvc.perform(get("/api/v1/contests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Contest A"))
    }

    @Test
    fun `create contest should return 201 when valid`() {
        val futureStart = Instant.now().plusSeconds(1000)
        val futureEnd = Instant.now().plusSeconds(5000)
        val request = CreateContestRequest(
            name = "Valid Contest",
            overview = "Overview text",
            rules = "Rules text",
            startTime = futureStart,
            freezeTime = null,
            endTime = futureEnd
        )

        val response = ContestDto(
            id = 10,
            name = request.name,
            overview = request.overview,
            rules = request.rules,
            startTime = request.startTime,
            freezeTime = null,
            endTime = request.endTime
        )

        every { contestService.createContest(any()) } returns response

        mockMvc.perform(post("/api/v1/contests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.name").value("Valid Contest"))
    }

    @ParameterizedTest(name = "Create Contest should fail when {1} is invalid")
    @MethodSource("invalidCreateContestProvider")
    fun `create contest should return 400 for invalid inputs`(
        invalidPayload: Map<String, Any?>,
        expectedErrorField: String
    ) {
        mockMvc.perform(post("/api/v1/contests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidPayload)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.$expectedErrorField").exists())
    }

    @Test
    fun `join contest should return member info`() {
        val expectedMember = ContestMemberDto(
            id = 55,
            userId = 1337,
            contestId = 5
        )

        every { contestService.joinContest(5, 1337) } returns expectedMember

        mockMvc.perform(post("/api/v1/contests/5/join"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(55))
            .andExpect(jsonPath("$.user_id").value(1337))
            .andExpect(jsonPath("$.contest_id").value(5))

        verify { contestService.joinContest(5, 1337) }
    }

    @Test
    fun `get contest progress should return progress details`() {
        val expectedProgress = ContestProgressDto(
            solvedCount = 2,
            totalUnsuccessful = 4,
            totalScore = 200
        )

        every { contestService.getUserProgress(5, 1337) } returns expectedProgress

        mockMvc.perform(get("/api/v1/contests/5/progress"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.solved_count").value(2))
            .andExpect(jsonPath("$.total_score").value(200))
    }

    companion object {
        @JvmStatic
        fun invalidCreateContestProvider(): Stream<Arguments> = Stream.of(
            // Past start time
            arguments(mapOf(
                "name" to "Past start time",
                "overview" to "Overview",
                "rules" to "Rules",
                "start_time" to Instant.now().minusSeconds(600).toString(),
                "end_time" to Instant.now().plusSeconds(600).toString()
            ), "start_time"),

            // Past end time
            arguments(mapOf(
                "name" to "Past end time",
                "overview" to "Overview",
                "rules" to "Rules",
                "start_time" to Instant.now().plusSeconds(600).toString(),
                "end_time" to Instant.now().minusSeconds(600).toString()
            ), "end_time"),

            // Too long name
            arguments(mapOf(
                "name" to "a".repeat(1001),
                "overview" to "Overview",
                "rules" to "Rules",
                "start_time" to Instant.now().plusSeconds(600).toString(),
                "end_time" to Instant.now().plusSeconds(1200).toString()
            ), "name")
        )
    }
}
