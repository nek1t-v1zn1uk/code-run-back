package com.example.coderun.domain.home.service

import com.example.coderun.domain.contest.dto.ContestDto
import com.example.coderun.domain.contest.repository.ContestRepository
import com.example.coderun.domain.home.dto.HomeRecommendationsDto
import com.example.coderun.domain.problems.dto.ProblemDto
import com.example.coderun.domain.problems.repository.ProblemRepository
import com.example.coderun.domain.solutions.repository.SolutionRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

import org.springframework.context.annotation.Lazy
import org.springframework.beans.factory.annotation.Autowired

@Service
class HomeRecommendationService(
    private val problemRepository: ProblemRepository,
    private val solutionRepository: SolutionRepository,
    private val contestRepository: ContestRepository
) {

    @Autowired
    @Lazy
    private lateinit var self: HomeRecommendationService

    @Transactional(readOnly = true)
    @Cacheable(value = ["trending_problems"], key = "'home'")
    fun getTrendingProblems(): List<ProblemDto> {
        val trendingProblemIds = solutionRepository.findTrendingProblemIds(PageRequest.of(0, 3))
        return problemRepository.findAllById(trendingProblemIds)
            .map { it.toProblemDto() }
    }

    @Transactional(readOnly = true)
    fun getRecommendations(userId: Int?): HomeRecommendationsDto {
        val now = Instant.now()
        
        val inProgressProblems = if (userId != null) {
            val problemIds = solutionRepository.findInProgressProblemIds(
                userId = userId,
                successStatus = com.example.coderun.domain.solutions.entity.SolutionStatus.SUCCESS,
                pageable = PageRequest.of(0, 3)
            )
            problemRepository.findAllById(problemIds).map { it.toProblemDto() }
        } else {
            emptyList()
        }

        val trendingProblems = self.getTrendingProblems()

        val activeContest = contestRepository.findFirstByStartTimeBeforeAndEndTimeAfterOrderByStartTimeAsc(now, now)?.toDto()
        val upcomingContest = contestRepository.findFirstByStartTimeAfterOrderByStartTimeAsc(now)?.toDto()

        val randomProblem = problemRepository.findRandomProblem()?.toProblemDto()

        return HomeRecommendationsDto(
            inProgressProblems = inProgressProblems,
            trendingProblems = trendingProblems,
            activeContest = activeContest,
            upcomingContest = upcomingContest,
            randomProblem = randomProblem
        )
    }
}
