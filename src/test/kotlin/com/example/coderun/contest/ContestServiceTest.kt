package com.example.coderun.contest

import com.example.coderun.domain.contest.entity.Contest
import com.example.coderun.domain.contest.entity.ContestMember
import com.example.coderun.domain.contest.repository.ContestMemberRepository
import com.example.coderun.domain.contest.repository.ContestProblemRepository
import com.example.coderun.domain.contest.repository.ContestRepository
import com.example.coderun.domain.contest.service.ContestService
import com.example.coderun.domain.problems.entity.Problem
import com.example.coderun.domain.problems.entity.ProblemDifficulty
import com.example.coderun.domain.problems.repository.ProblemRepository
import com.example.coderun.domain.solutions.entity.Solution
import com.example.coderun.domain.solutions.entity.SolutionStatus
import com.example.coderun.domain.solutions.entity.AvailableLanguage
import com.example.coderun.domain.solutions.repository.SolutionRepository
import com.example.coderun.domain.users.User
import com.example.coderun.domain.users.UserRepository
import com.example.coderun.domain.users.UserRoles
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class ContestServiceTest {

    private val contestRepository: ContestRepository = mockk()
    private val contestProblemRepository: ContestProblemRepository = mockk()
    private val contestMemberRepository: ContestMemberRepository = mockk()
    private val problemRepository: ProblemRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val solutionRepository: SolutionRepository = mockk()

    private lateinit var contestService: ContestService

    @BeforeEach
    fun setUp() {
        contestService = ContestService(
            contestRepository,
            contestProblemRepository,
            contestMemberRepository,
            problemRepository,
            userRepository,
            solutionRepository
        )
    }

    @Test
    fun `joinContest should throw IllegalArgumentException when user already joined`() {
        every { contestMemberRepository.existsByContestIdAndUserId(1, 100) } returns true

        assertThrows<IllegalArgumentException> {
            contestService.joinContest(1, 100)
        }

        verify { contestMemberRepository.existsByContestIdAndUserId(1, 100) }
    }

    @Test
    fun `joinContest should save and return member when new`() {
        val contest = Contest(id = 1, name = "C1", startTime = Instant.now(), endTime = Instant.now().plusSeconds(3600))
        val user = User(id = 100, email = "u@example.com", passwordHash = "hash", firstName = "Bob", role = UserRoles.USER)

        every { contestMemberRepository.existsByContestIdAndUserId(1, 100) } returns false
        every { contestRepository.findById(1) } returns Optional.of(contest)
        every { userRepository.findById(100) } returns Optional.of(user)
        every { contestMemberRepository.save(any()) } answers {
            val member = firstArg<ContestMember>()
            member.id = 500
            member
        }

        val result = contestService.joinContest(1, 100)

        assertNotNull(result)
        assertEquals(500, result.id)
        assertEquals(1, result.contestId)
        assertEquals(100, result.userId)

        verify { contestMemberRepository.save(any()) }
    }

    @Test
    fun `getUserProgress should calculate correct statistics`() {
        val startTime = Instant.now().minusSeconds(1800) // 30 mins ago
        val contest = Contest(id = 1, name = "C1", startTime = startTime, endTime = Instant.now().plusSeconds(3600))
        val problem = Problem(id = 10, title = "P1", difficulty = ProblemDifficulty.EASY, statement = "S1", executionTimeLimitMs = 1000, executionMemoryLimitKb = 1024)
        val language = AvailableLanguage(id = 1, language = "python", version = "3.10")

        val solution1 = Solution(
            id = 101, problem = problem, user = mockk(), code = "c", language = language,
            contest = contest, status = SolutionStatus.TEST_FAILED, sentAt = startTime.plusSeconds(300)
        )
        val solution2 = Solution(
            id = 102, problem = problem, user = mockk(), code = "c", language = language,
            contest = contest, status = SolutionStatus.SUCCESS, sentAt = startTime.plusSeconds(600) // 10 mins penalty + 20 mins failure penalty
        )

        every { contestRepository.findById(1) } returns Optional.of(contest)
        every { solutionRepository.findAllByContestIdAndUserIdOrderBySentAtAsc(1, 100) } returns listOf(solution1, solution2)

        val progress = contestService.getUserProgress(1, 100)

        assertEquals(1, progress.solvedCount)
        assertEquals(1, progress.totalUnsuccessful)
        // 10 minutes time penalty + 1 unsuccessful * 20 penalty = 30 points
        assertEquals(30, progress.totalScore)
        assertTrue(progress.problemStats.containsKey(10))
        assertEquals(30, progress.problemStats[10]?.score)
        assertTrue(progress.problemStats[10]?.isSolved == true)
    }
}
