package com.example.coderun.domain.contest.repository

import com.example.coderun.domain.contest.entity.Contest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContestRepository : JpaRepository<Contest, Int> {
    fun findByEndTimeBeforeAndResultsCalculatedFalse(endTime: java.time.Instant): List<Contest>
}
