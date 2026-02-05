package com.example.coderun.domain.tests

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestRepository : JpaRepository<Test, Int> {

}
