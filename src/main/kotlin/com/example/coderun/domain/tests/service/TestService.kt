package com.example.coderun.domain.tests.service

import com.example.coderun.domain.problems.repository.ProblemRepository
import com.example.coderun.domain.tests.dto.UpdateTestListRequest
import com.example.coderun.domain.tests.entity.Test
import com.example.coderun.domain.tests.repository.TestRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class TestService(
    private val problemRepository: ProblemRepository,
    private val testRepository: TestRepository,
) {
    @Transactional
    fun updateTestList(problemId: Int, request: UpdateTestListRequest) {
        val problem = problemRepository.findById(problemId).getOrNull()
            ?: throw EntityNotFoundException("Problem with id: $problemId not found")

        request.deleteTests.forEach {
            testRepository.deleteByIdAndProblemId(it.id!!, problemId)
        }
        testRepository.flush()

        val updatedTests = request.updateTests.mapNotNull { req ->
            testRepository.findById(req.id!!).getOrNull()?.apply {
                if (this.problem!!.id == problemId) {
                    req.ordinal?.let { this.ordinal = it }
                    req.isExample?.let { this.isExample = it }
                    req.inputData?.let { this.inputData = it }
                    req.expectedOutput?.let { this.expectedOutput = it }
                    req.overrideEvaluationType?.let { this.overrideEvaluationType = it }
                }
            }
        }
        testRepository.saveAllAndFlush(updatedTests)

        val newTests = request.newTests.map {
            Test(
                problem = problem,
                ordinal = it.ordinal!!,
                isExample = it.isExample!!,
                inputData = it.inputData!!,
                expectedOutput = it.expectedOutput,
                overrideEvaluationType = it.overrideEvaluationType,
            )
        }
        testRepository.saveAll(newTests)
    }
}