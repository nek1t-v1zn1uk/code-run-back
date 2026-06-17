package com.example.coderun.domain.home.dto

import com.example.coderun.domain.contest.dto.ContestDto
import com.example.coderun.domain.problems.dto.ProblemDto

data class HomeRecommendationsDto(
    val inProgressProblems: List<ProblemDto>,
    val trendingProblems: List<ProblemDto>,
    val activeContest: ContestDto?,
    val upcomingContest: ContestDto?,
    val randomProblem: ProblemDto?
)
