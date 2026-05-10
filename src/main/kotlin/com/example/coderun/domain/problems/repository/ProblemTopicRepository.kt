package com.example.coderun.domain.problems.repository

import com.example.coderun.domain.problems.entity.ProblemTopic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProblemTopicRepository : JpaRepository<ProblemTopic, String> {
    fun findByName(name: String): ProblemTopic?
}
