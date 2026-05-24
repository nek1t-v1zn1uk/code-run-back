package com.example.coderun.domain.solutions.repository

import com.example.coderun.domain.solutions.entity.Solution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SolutionRepository : JpaRepository<Solution, Int> {
    fun findAllByProblemIdAndUserIdOrderBySentAtDesc(problemId: Int, userId: Int): List<Solution>
}