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
import com.example.coderun.domain.solutions.repository.SolutionRepository
import com.example.coderun.domain.solutions.entity.SolutionStatus
import java.time.Duration
import kotlin.jvm.optionals.getOrNull

@Service
class ContestService(
    private val contestRepository: ContestRepository,
    private val contestProblemRepository: ContestProblemRepository,
    private val contestMemberRepository: ContestMemberRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
    private val solutionRepository: SolutionRepository
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

    fun hasJoinedContest(contestId: Int, userId: Int): Boolean {
        return contestMemberRepository.existsByContestIdAndUserId(contestId, userId)
    }

    @Transactional
    fun joinContest(contestId: Int, userId: Int): ContestMemberDto {
        if (hasJoinedContest(contestId, userId)) {
            throw IllegalArgumentException("User has already joined this contest")
        }

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

    fun getUserProgress(contestId: Int, userId: Int): ContestProgressDto {
        val contest = contestRepository.findById(contestId).getOrNull()
            ?: throw EntityNotFoundException("Contest not found")
            
        val solutions = solutionRepository.findAllByContestIdAndUserIdOrderBySentAtAsc(contestId, userId)
        val solutionsByProblem = solutions.groupBy { it.problem.id!! }
        
        var totalSolved = 0
        var totalUnsuccessful = 0
        var totalScore = 0
        val problemStats = mutableMapOf<Int, ProblemStatDto>()
        
        for ((problemId, problemSolutions) in solutionsByProblem) {
            var isSolved = false
            var unsuccessfulCount = 0
            var score = 0
            
            for (solution in problemSolutions) {
                if (solution.status == SolutionStatus.SUCCESS) {
                    isSolved = true
                    val minutesFromStart = Duration.between(contest.startTime, solution.sentAt).toMinutes().toInt()
                    val timePenalty = maxOf(0, minutesFromStart)
                    score = timePenalty + (unsuccessfulCount * 20)
                    break
                } else if (solution.status != SolutionStatus.IN_QUEUE && 
                           solution.status != SolutionStatus.COMPILING && 
                           solution.status != SolutionStatus.EXECUTION) {
                    unsuccessfulCount++
                }
            }
            
            if (isSolved) {
                totalSolved++
                totalUnsuccessful += unsuccessfulCount
                totalScore += score
            }
            
            problemStats[problemId] = ProblemStatDto(
                problemId = problemId,
                isSolved = isSolved,
                unsuccessfulCount = unsuccessfulCount,
                score = score
            )
        }
        
        return ContestProgressDto(
            solvedCount = totalSolved,
            totalUnsuccessful = totalUnsuccessful,
            totalScore = totalScore,
            problemStats = problemStats
        )
    }

    fun getScoreboard(contestId: Int): ScoreboardDto {
        val contest = contestRepository.findById(contestId).getOrNull()
            ?: throw EntityNotFoundException("Contest not found")
            
        val members = contestMemberRepository.findAllByContestId(contestId)
        val solutions = solutionRepository.findAllByContestIdOrderBySentAtAsc(contestId)
        
        val solutionsByUser = solutions.groupBy { it.user.id!! }
        
        val rows = members.map { member ->
            val userSolutions = solutionsByUser[member.user.id!!] ?: emptyList()
            val solutionsByProblem = userSolutions.groupBy { it.problem.id!! }
            
            var totalSolved = 0
            var totalScore = 0
            val problemStats = mutableMapOf<Int, ScoreboardProblemStatDto>()
            
            for ((problemId, problemSolutions) in solutionsByProblem) {
                var isSolved = false
                var unsuccessfulCount = 0
                var frozenAttempts = 0
                var score = 0
                
                for (solution in problemSolutions) {
                    val isFrozen = contest.freezeTime != null && solution.sentAt >= contest.freezeTime
                    
                    if (isFrozen) {
                        if (!isSolved) {
                            frozenAttempts++
                        }
                    } else {
                        if (solution.status == SolutionStatus.SUCCESS) {
                            isSolved = true
                            val minutesFromStart = Duration.between(contest.startTime, solution.sentAt).toMinutes().toInt()
                            val timePenalty = maxOf(0, minutesFromStart)
                            score = timePenalty + (unsuccessfulCount * 20)
                            break
                        } else if (solution.status != SolutionStatus.IN_QUEUE && 
                                   solution.status != SolutionStatus.COMPILING && 
                                   solution.status != SolutionStatus.EXECUTION) {
                            unsuccessfulCount++
                        }
                    }
                }
                
                if (isSolved) {
                    totalSolved++
                    totalScore += score
                }
                
                problemStats[problemId] = ScoreboardProblemStatDto(
                    problemId = problemId,
                    isSolved = isSolved,
                    unsuccessfulCount = unsuccessfulCount,
                    frozenAttempts = frozenAttempts,
                    score = score
                )
            }
            
            ScoreboardRowDto(
                userId = member.user.id!!,
                username = if (member.user.lastName != null) "${member.user.firstName} ${member.user.lastName}" else member.user.firstName,
                solvedCount = totalSolved,
                totalScore = totalScore,
                problemStats = problemStats
            )
        }
        
        val sortedRows = rows.sortedWith(
            compareByDescending<ScoreboardRowDto> { it.solvedCount }
                .thenBy { it.totalScore }
        )
        
        var currentPlace = 1
        var previousRow: ScoreboardRowDto? = null
        val finalRows = sortedRows.mapIndexed { index, row ->
            if (previousRow != null && previousRow!!.solvedCount == row.solvedCount && previousRow!!.totalScore == row.totalScore) {
                // same place as previous
                row.copy(place = currentPlace)
            } else {
                currentPlace = index + 1
                previousRow = row
                row.copy(place = currentPlace)
            }
        }
        
        return ScoreboardDto(
            contestId = contestId,
            rows = finalRows
        )
    }
}
