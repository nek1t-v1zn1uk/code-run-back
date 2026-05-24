package com.example.coderun.domain.contest.repository

import com.example.coderun.domain.contest.entity.ContestProblem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContestProblemRepository : JpaRepository<ContestProblem, Int> {
    fun findAllByContestId(contestId: Int): List<ContestProblem>
    fun findByContestIdAndProblemId(contestId: Int, problemId: Int): ContestProblem?
    fun deleteAllByContestId(contestId: Int)
}
