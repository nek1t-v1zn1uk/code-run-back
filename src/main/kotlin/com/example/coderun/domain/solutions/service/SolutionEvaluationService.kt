package com.example.coderun.domain.solutions.service

import com.example.coderun.domain.isolaterunner.IsolateService
import com.example.coderun.domain.problems.entity.EvaluationType
import com.example.coderun.domain.solutions.entity.Solution
import com.example.coderun.domain.solutions.entity.SolutionStatus
import com.example.coderun.domain.solutions.repository.SolutionRepository
import com.example.coderun.domain.tests.repository.ScriptCheckerRepository
import com.example.coderun.domain.contest.event.SolutionEvaluatedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant
import java.util.LinkedList
import java.util.Queue
import kotlin.math.roundToInt
import java.util.concurrent.CompletableFuture

@Service
class SolutionEvaluationService(
    private val isolateService: IsolateService,
    private val solutionRepository: SolutionRepository,
    private val broadcastService: SolutionBroadcastService,
    private val eventPublisher: ApplicationEventPublisher,
    private val transactionTemplate: TransactionTemplate,

    private val queue: Queue<Int> = LinkedList(),
) {
    @Synchronized
    fun enqueueSolution(solutionId: Int) {
        println("ENQUEUING SOLUTION: $solutionId")
        val wasEmpty = queue.isEmpty()
        queue.add(solutionId)
        println("QUEUE SIZE IS NOW: ${queue.size}, WAS EMPTY: $wasEmpty")
        if (wasEmpty) {
            println("STARTING ASYNC EVALUATION THREAD")
            CompletableFuture.runAsync {
                println("ASYNC THREAD STARTED")
                evaluateNextSolution()
            }
        }
    }

    fun evaluateSolution(solutionId: Int) {
        println("EVALUATING SOLUTION ID: $solutionId")
        var solution: Solution? = null
        var problem: com.example.coderun.domain.problems.entity.Problem? = null
        var tests: MutableList<com.example.coderun.domain.tests.entity.Test>? = null

        try {
            transactionTemplate.execute {
                solution = solutionRepository.findById(solutionId).orElse(null)
                if (solution != null) {
                    problem = solution!!.problem
                    tests = problem!!.tests.toMutableList()
                    // Initialize lazy fields required for toDto()
                    solution!!.user.firstName
                    solution!!.language.language
                    solution!!.contest?.id
                    solution!!.contestProblem?.id
                    
                    // Initialize script checkers if needed
                    problem!!.defaultScriptChecker?.code
                    problem!!.defaultScriptChecker?.language?.language
                    tests!!.forEach { 
                        it.overrideScriptChecker?.code
                        it.overrideScriptChecker?.language?.language
                    }
                }
            }

            if (solution == null || problem == null || tests == null) {
                println("SOLUTION/PROBLEM/TESTS IS NULL for ID: $solutionId. Solution=$solution, Problem=$problem, Tests=$tests")
                return
            }

            println("FETCHED SOLUTION SUCCESSFULLY. UPDATING STATUS TO COMPILING")
            tests!!.sortBy { it.ordinal }

            // Broadcast COMPILING status
            solution!!.status = SolutionStatus.COMPILING
            solutionRepository.save(solution!!)
            broadcastService.broadcastSolutionUpdate(solution!!)
            
            println("COMPILING...")

            val binaryPath = try {
                isolateService.compileCode(solution!!.code!!, solution!!.language.language)
            } catch (e: Exception) {
                println("COMPILATION THREW EXCEPTION: ${e.message}")
                e.printStackTrace()
                solution!!.status = SolutionStatus.COMPILATION_ERROR
                solution!!.executedAt = Instant.now()
                solutionRepository.save(solution!!)
                broadcastService.broadcastSolutionUpdate(solution!!)
                if (solution!!.contest != null) {
                    eventPublisher.publishEvent(SolutionEvaluatedEvent(this, solution!!.contest!!.id!!))
                }
                return
            }
            
            println("COMPILATION SUCCESSFUL. EXECUTING TESTS...")

        var index = 0
        for(test in tests!!) {
            index++
            
            solution!!.status = SolutionStatus.EXECUTION
            solution!!.testCaseReached = index
            solutionRepository.save(solution!!)
            broadcastService.broadcastSolutionUpdate(solution!!)
            
            val solutionResult = try {
                isolateService.executeCompiledCode(
                    binaryPath,
                    solution!!.language.language,
                    test.inputData,
                    problem!!.executionTimeLimitMs / 1000F,
                    problem!!.executionMemoryLimitKb
                )
            } catch (e: Exception) {
                solution!!.status = SolutionStatus.INTERNAL_ERROR
                solution!!.testCaseReached = index
                break
            }

            if(solutionResult.status == "OK"){
                val evaluationType = test.overrideEvaluationType ?: problem.defaultEvaluationType!!
                if(evaluationType == EvaluationType.EXACT_MATCH) {
                    if(!test.expectedOutput.equals(solutionResult.stdout.trim())) {
                        solution.status = SolutionStatus.TEST_FAILED
                        solution.testCaseReached = index
                        break
                    }
                } else if (evaluationType == EvaluationType.SCRIPT_CHECK) {
                    val scriptChecker = test.overrideScriptChecker ?: problem.defaultScriptChecker!!
                    val scriptCheckerPath = isolateService.compileCode(scriptChecker.code, scriptChecker.language.language)
                    val checkerResult = isolateService.executeCompiledCode(
                        scriptCheckerPath,
                        scriptChecker.language.language,
                        solutionResult.stdout.trim() + "\n" + test.inputData + "\n" + test.expectedOutput,
                        10F,
                        1024*1024
                    )
                    java.nio.file.Files.deleteIfExists(scriptCheckerPath)
                    
                    if(checkerResult.status != "OK" || checkerResult.stdout.trim() == "False") {
                        solution.status = SolutionStatus.TEST_FAILED
                        solution.testCaseReached = index
                        break
                    }
                }
            } else {
                solution.status = when(solutionResult.status) {
                    "TO" -> {
                        solution.executionTimeMs = (solutionResult.time*1000).roundToInt()
                        SolutionStatus.TIME_LIMIT_EXCEEDED
                    }
                    "RE" -> SolutionStatus.RUNTIME_ERROR
                    "SG" -> {
                        when (solutionResult.exitSignal) {
                            9 -> {
                                solution.executionMemoryKb = solutionResult.memory.toInt()
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
        
            java.nio.file.Files.deleteIfExists(binaryPath)
            
            if(index == tests!!.size) {
                solution!!.status = SolutionStatus.SUCCESS
                solution!!.testCaseReached = null
            }
            solution!!.executedAt = Instant.now()

            solutionRepository.save(solution!!)
            broadcastService.broadcastSolutionUpdate(solution!!)
            
            if (solution!!.contest != null) {
                eventPublisher.publishEvent(SolutionEvaluatedEvent(this, solution!!.contest!!.id!!))
            }
        } catch (e: Exception) {
            println("EXCEPTION IN EVALUATE SOLUTION: ${e.message}")
            e.printStackTrace()
            if (solution != null) {
                try {
                    solution!!.status = SolutionStatus.INTERNAL_ERROR
                    solutionRepository.save(solution!!)
                    broadcastService.broadcastSolutionUpdate(solution!!)
                } catch (ignored: Exception) {
                    println("FAILED TO SET INTERNAL ERROR STATUS: ${ignored.message}")
                }
            }
        } finally {
            println("FINALLY BLOCK REACHED. CALLING evaluateNextSolution()")
            evaluateNextSolution()
        }
    }

    @Synchronized
    private fun pollNextSolution(): Int? {
        val next = queue.poll()
        println("POLLED NEXT SOLUTION: $next")
        return next
    }

    fun evaluateNextSolution() {
        println("EVALUATE NEXT SOLUTION CALLED")
        val solutionId = pollNextSolution() ?: return
        evaluateSolution(solutionId)
    }

}