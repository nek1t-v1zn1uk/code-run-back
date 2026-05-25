package com.example.coderun.domain.contest.service

import com.example.coderun.domain.contest.dto.*
import com.example.coderun.domain.contest.entity.*
import com.example.coderun.domain.contest.repository.*
import com.example.coderun.domain.problems.repository.ProblemRepository
import com.example.coderun.domain.users.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import kotlin.jvm.optionals.getOrNull

@Service
class ContestService(
    private val contestRepository: ContestRepository,
    private val contestProblemRepository: ContestProblemRepository,
    private val contestMemberRepository: ContestMemberRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository
) {
    fun getAllContests(): List<ContestDto> {
        return contestRepository.findAll().map { it.toDto() }
    }

    fun getContestById(id: Int): ContestDto {
        val contest = contestRepository.findById(id).getOrNull()
            ?: throw EntityNotFoundException("Contest not found")

        return contest.toDto()
    }

    @Transactional
    fun createContest(request: CreateContestRequest): ContestDto {
        val contest = Contest(
            name = request.name,
            overview = request.overview,
            rules = request.rules,
            startTime = request.startTime,
            freezeTime = request.freezeTime,
            endTime = request.endTime
        )
        return contestRepository.save(contest).toDto()
    }

    @Transactional
    fun updateContest(id: Int, request: UpdateContestRequest): ContestDto {
        val contest = contestRepository.findById(id).getOrNull()
            ?: throw EntityNotFoundException("Contest not found")

        request.name?.let { contest.name = it }
        request.overview?.let { contest.overview = it }
        request.rules?.let { contest.rules = it }
        request.startTime?.let { contest.startTime = it }
        request.freezeTime?.let { contest.freezeTime = it }
        request.endTime?.let { contest.endTime = it }

        return contestRepository.save(contest).toDto()
    }
    
    fun getContestProblems(contestId: Int): List<ContestProblemDto> {
        return contestProblemRepository.findAllByContestId(contestId).map { it.toDto() }
    }

    @Transactional
    fun addProblemToContest(contestId: Int, request: AddContestProblemRequest): ContestProblemDto {
        val contest = contestRepository.findById(contestId).getOrNull()
            ?: throw EntityNotFoundException("Contest not found")

        val problem = problemRepository.findById(request.problemId).getOrNull()
            ?: throw EntityNotFoundException("Problem not found")
        
        val contestProblem = ContestProblem(
            contest = contest,
            problem = problem,
            ordinal = request.ordinal
        )
        return contestProblemRepository.save(contestProblem).toDto()
    }

    @Transactional
    fun updateContestProblems(contestId: Int, request: UpdateContestProblemsRequest): List<ContestProblemDto> {
        val contest = contestRepository.findById(contestId).getOrNull()
            ?: throw EntityNotFoundException("Contest not found")

        // Remove all existing problems for this contest
        contestProblemRepository.deleteAllByContestId(contestId)
        contestProblemRepository.flush()

        // Insert new order
        val newProblems = request.problemIds.mapIndexed { index, probId ->
            val problem = problemRepository.findById(probId).getOrNull()
                ?: throw EntityNotFoundException("Problem ID $probId not found")
            ContestProblem(
                contest = contest,
                problem = problem,
                ordinal = index + 1 // 1-indexed
            )
        }
        
        return contestProblemRepository.saveAll(newProblems).map { it.toDto() }
    }

    fun getContestMembers(contestId: Int): List<ContestMemberDto> {
        return contestMemberRepository.findAllByContestId(contestId).map { it.toDto() }
    }

    fun hasJoinedContest(contestId: Int): Boolean {
        val authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication!!
        val user = authentication.principal as com.example.coderun.domain.users.User
        return contestMemberRepository.existsByContestIdAndUserId(contestId, user.id!!)
    }

    @Transactional
    fun joinContest(contestId: Int, userId: Int): ContestMemberDto {
        if (contestMemberRepository.existsByContestIdAndUserId(contestId, userId))
            throw IllegalArgumentException("Already joined")

        val contest = contestRepository.findById(contestId).getOrNull()
            ?: throw EntityNotFoundException("Contest not found")

        val user = userRepository.findById(userId).getOrNull()
            ?: throw EntityNotFoundException("User not found")

        val member = ContestMember(
            contest = contest,
            user = user
        )
        return contestMemberRepository.save(member).toDto()
    }
}
