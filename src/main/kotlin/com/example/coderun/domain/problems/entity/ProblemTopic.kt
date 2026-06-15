package com.example.coderun.domain.problems.entity

import jakarta.persistence.*

@Entity
@Table(name = "problem_topics")
data class ProblemTopic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false, unique = true)
    var name: String
)
