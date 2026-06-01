package com.example.coderun.domain.contest.entity

import com.example.coderun.domain.contest.dto.ContestProblemDto
import com.example.coderun.domain.problems.entity.Problem
import jakarta.persistence.*

@Entity
@Table(
    name = "contest_problems",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["contest_id", "ordinal"]),
        UniqueConstraint(columnNames = ["contest_id", "problem_id"])
    ]
)
data class ContestProblem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    var contest: Contest,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: Problem,

    @Column(nullable = false)
    var ordinal: Int
) {
    fun toDto() = ContestProblemDto(
        id = this.id!!,
        contestId = this.contest.id!!,
        problemId = this.problem.id!!,
        ordinal = this.ordinal
    )
}
