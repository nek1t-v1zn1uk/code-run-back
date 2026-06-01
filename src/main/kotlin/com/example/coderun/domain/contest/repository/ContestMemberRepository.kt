package com.example.coderun.domain.contest.repository

import com.example.coderun.domain.contest.entity.ContestMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContestMemberRepository : JpaRepository<ContestMember, Int> {
    fun findAllByContestId(contestId: Int): List<ContestMember>
    fun findByContestIdAndUserId(contestId: Int, userId: Int): ContestMember?
    fun existsByContestIdAndUserId(contestId: Int, userId: Int): Boolean
}
