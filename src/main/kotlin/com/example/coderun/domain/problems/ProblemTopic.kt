package com.example.coderun.domain.problems

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "problem_topics")
data class ProblemTopic(
    @Id
    var name: String
)
