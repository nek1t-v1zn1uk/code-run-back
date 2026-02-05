package com.example.coderun.domain.problems

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProblemTopicRepository : JpaRepository<ProblemTopic, String> {

}
