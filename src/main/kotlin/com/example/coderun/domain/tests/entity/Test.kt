package com.example.coderun.domain.tests.entity

import com.example.coderun.domain.problems.entity.EvaluationType
import com.example.coderun.domain.problems.entity.Problem
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
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

    @Column(name = "override_evaluation_type", columnDefinition = "evaluation_types")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    var overrideEvaluationType: EvaluationType? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "override_script_checker_id")
    var overrideScriptChecker: ScriptChecker? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)