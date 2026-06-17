package com.example.coderun.domain.solutions.repository

import com.example.coderun.domain.solutions.entity.Solution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SolutionRepository : JpaRepository<Solution, Int> {
    fun findAllByProblemIdAndUserIdOrderBySentAtDesc(problemId: Int, userId: Int): List<Solution>
    fun findAllByContestIdAndUserIdOrderBySentAtAsc(contestId: Int, userId: Int): List<Solution>
    fun findAllByContestIdOrderBySentAtAsc(contestId: Int): List<Solution>

    @Query("SELECT s.problem.id FROM Solution s GROUP BY s.problem.id ORDER BY COUNT(s.id) DESC")
    fun findTrendingProblemIds(pageable: org.springframework.data.domain.Pageable): List<Int>

    @Query("SELECT DISTINCT s.problem.id FROM Solution s WHERE s.user.id = :userId AND NOT EXISTS (SELECT s2 FROM Solution s2 WHERE s2.problem.id = s.problem.id AND s2.user.id = :userId AND s2.status = :successStatus) ORDER BY s.problem.id DESC")
    fun findInProgressProblemIds(
        @org.springframework.data.repository.query.Param("userId") userId: Int,
        @org.springframework.data.repository.query.Param("successStatus") successStatus: com.example.coderun.domain.solutions.entity.SolutionStatus,
        pageable: org.springframework.data.domain.Pageable
    ): List<Int>
}