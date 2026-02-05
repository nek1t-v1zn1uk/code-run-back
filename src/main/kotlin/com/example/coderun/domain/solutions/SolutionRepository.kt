package com.example.coderun.domain.solutions

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SolutionRepository : JpaRepository<Solution, Int> {

}
