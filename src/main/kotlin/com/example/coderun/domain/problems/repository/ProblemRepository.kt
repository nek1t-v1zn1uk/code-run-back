package com.example.coderun.domain.problems.repository

import com.example.coderun.domain.problems.entity.Problem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface ProblemRepository : JpaRepository<Problem, Int>, JpaSpecificationExecutor<Problem> {

}
