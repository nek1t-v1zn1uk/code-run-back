package com.example.coderun.domain.tests.repository

import com.example.coderun.domain.tests.entity.Test
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestRepository : JpaRepository<Test, Int> {

}