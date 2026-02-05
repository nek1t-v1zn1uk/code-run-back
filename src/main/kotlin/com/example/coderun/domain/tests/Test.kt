package com.example.coderun.domain.tests

import com.example.coderun.domain.problems.EvaluationType
import com.example.coderun.domain.problems.Problem
import com.example.coderun.domain.problems.ScriptChecker
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "tests", uniqueConstraints = [UniqueConstraint(columnNames = ["problem_id", "ordinal"])])
data class Test(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    var problem: Problem? = null,

    @Column(nullable = false)
    var ordinal: Int,

    @Column(name = "is_example", nullable = false)
    var isExample: Boolean = false,

    @Column(name = "input_data", nullable = false)
    var inputData: String,

    @Column(name = "expected_output")
    var expectedOutput: String? = null,

    @Column(name = "override_evaluation_type")
    var overrideEvaluationType: EvaluationType? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "override_script_checker_id")
    var overrideScriptChecker: ScriptChecker? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
