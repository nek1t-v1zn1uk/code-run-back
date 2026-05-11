package com.example.coderun.domain.solutions.entity

import com.example.coderun.domain.problems.entity.Problem
import com.example.coderun.domain.solutions.dto.SolutionDto
import com.example.coderun.domain.users.User
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "solutions")
data class Solution(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: Problem,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column
    var code: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    var language: AvailableLanguage,

    @Column(nullable = false, columnDefinition = "solution_statuses")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    var status: SolutionStatus = SolutionStatus.IN_QUEUE,

    @Column(name = "test_case_reached")
    var testCaseReached: Int? = null,

    @Column(name = "execution_time_ms")
    var executionTimeMs: Int? = null,

    @Column(name = "execution_memory_kb")
    var executionMemoryKb: Int? = null,

    @Column(name = "sent_at", nullable = false)
    var sentAt: Instant = Instant.now(),

    @Column(name = "executed_at")
    var executedAt: Instant? = null
) {
    fun toDto() = SolutionDto(
        id = this.id!!,
        problemId = this.problem.id!!,
        userId = this.user.id!!,
        code = this.code!!,
        language = this.language,
        testCaseReached = this.testCaseReached,
        executionTimeMs = this.executionTimeMs,
        executionMemoryKb = this.executionMemoryKb,
        sentAt = this.sentAt,
        executedAt = this.executedAt
    )
}
