package com.example.coderun.domain.problems

import jakarta.persistence.*
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

    @Column(nullable = false)
    var difficulty: ProblemDifficulty,

    @Column(nullable = false)
    var statement: String,

    @Column(name = "execution_time_limit_ms", nullable = false)
    var executionTimeLimitMs: Int,

    @Column(name = "execution_memory_limit_kb", nullable = false)
    var executionMemoryLimitKb: Int,

    @Column(name = "default_evaluation_type")
    var defaultEvaluationType: EvaluationType? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_script_checker_id")
    var defaultScriptChecker: ScriptChecker? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
