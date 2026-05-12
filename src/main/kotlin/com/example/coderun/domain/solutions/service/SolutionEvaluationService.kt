package com.example.coderun.domain.solutions.service

import com.example.coderun.domain.isolaterunner.IsolateService
import com.example.coderun.domain.problems.entity.EvaluationType
import com.example.coderun.domain.solutions.entity.Solution
import com.example.coderun.domain.solutions.entity.SolutionStatus
import com.example.coderun.domain.solutions.repository.SolutionRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.LinkedList
import java.util.Queue
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Service
class SolutionEvaluationService(
    private val isolateService: IsolateService,
    private val solutionRepository: SolutionRepository,

    private val queue: Queue<Solution> = LinkedList(),
) {
    fun enqueueSolution(solution: Solution) {
        if (queue.isEmpty())
            evaluateSolution(solution)
        else
            queue.add(solution)
    }

    fun evaluateSolution(solution: Solution) {
        val problem = solution.problem
        val tests = problem.tests
        tests.sortBy { it.ordinal }

        var index = 0
        for(test in tests) {
            index++
            val result = isolateService.executeCode(
                solution.code!!,
                solution.language.language,
                test.inputData,
                problem.executionTimeLimitMs / 1000F,
                problem.executionMemoryLimitKb
            )
            val evaluationType = test.overrideEvaluationType ?: problem.defaultEvaluationType!!
            if(evaluationType == EvaluationType.EXACT_MATCH) {
                if(result.status == "OK"){
                    if(!test.expectedOutput.equals(result.stdout.trim())) {
                        solution.status = SolutionStatus.TEST_FAILED
                        solution.testCaseReached = index
                        break
                    }
                } else {
                    println(result)
                    solution.status = when(result.status) {
                        "TO" -> {
                            solution.executionTimeMs = (result.time*1000).roundToInt()
                            SolutionStatus.TIME_LIMIT_EXCEEDED
                        }
                        "RE" -> SolutionStatus.RUNTIME_ERROR
                        "SG" -> {
                            when (result.exitSignal) {
                                9 -> {
                                    solution.executionMemoryKb = result.memory.toInt()
                                    SolutionStatus.MEMORY_LIMIT_EXCEEDED
                                }
                                25 -> SolutionStatus.OUTPUT_LIMIT_EXCEEDED // doesnt work for now
                                else -> SolutionStatus.INTERNAL_ERROR
                            }
                        }
                        else -> SolutionStatus.INTERNAL_ERROR
                    }
                    solution.testCaseReached = index
                    break
                }
            }
        }
        if(index == tests.size)
            solution.status = SolutionStatus.SUCCESS
        solution.executedAt = Instant.now()

        solutionRepository.save(solution)

        evaluateNextSolution()
    }

    fun evaluateNextSolution() {
        if(queue.isNotEmpty())
            evaluateSolution(queue.poll())
    }

}