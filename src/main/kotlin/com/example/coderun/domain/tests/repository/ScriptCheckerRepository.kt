package com.example.coderun.domain.tests.repository

import com.example.coderun.domain.tests.entity.ScriptChecker
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScriptCheckerRepository : JpaRepository<ScriptChecker, Int> {

}