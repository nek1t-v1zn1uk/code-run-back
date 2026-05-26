package com.example.coderun.domain.problems.entity

import com.example.coderun.domain.problems.dto.ProblemDto
import com.example.coderun.domain.tests.entity.ScriptChecker
import com.example.coderun.domain.tests.entity.Test
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "problems")
data class Problem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false)
    var title: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic")
    var topic: ProblemTopic? = null,

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    var difficulty: ProblemDifficulty,

    @Column(nullable = false)
    var statement: String,

    @Column(name = "execution_time_limit_ms", nullable = false)
    var executionTimeLimitMs: Int,

    @Column(name = "execution_memory_limit_kb", nullable = false)
    var executionMemoryLimitKb: Int,

    @Column(name = "default_evaluation_type", columnDefinition = "evaluation_types")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    var defaultEvaluationType: EvaluationType? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_script_checker_id")
    var defaultScriptChecker: ScriptChecker? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = true,

    @OneToMany(mappedBy = "problem", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    val tests: MutableList<Test> = mutableListOf()
) {
    fun toProblemDto() = ProblemDto(
        id!!,
        title,
        topic,
        difficulty,
        statement,
        executionTimeLimitMs,
        executionMemoryLimitKb,
        defaultEvaluationType,
        defaultScriptChecker?.id,
        createdAt,
        isPublic
    )
}
