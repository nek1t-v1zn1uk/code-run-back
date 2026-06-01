package com.example.coderun.domain.problems.service

import com.example.coderun.domain.problems.dto.CreateProblemRequest
import com.example.coderun.domain.problems.dto.GetProblemsRequest
import com.example.coderun.domain.problems.dto.ProblemCursor
import com.example.coderun.domain.problems.dto.ProblemDto
import com.example.coderun.domain.problems.dto.ProblemPageResponse
import com.example.coderun.domain.problems.dto.UpdateProblemRequest
import com.example.coderun.domain.problems.entity.Problem
import com.example.coderun.domain.problems.entity.ProblemDifficulty
import com.example.coderun.domain.problems.entity.ProblemTopic
import com.example.coderun.domain.problems.repository.ProblemRepository
import com.example.coderun.domain.problems.repository.ProblemTopicRepository
import com.example.coderun.domain.tests.repository.ScriptCheckerRepository
import com.example.coderun.util.CursorUtil
import jakarta.persistence.EntityNotFoundException
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class ProblemService (
    private val problemRepository: ProblemRepository,
    private val problemTopicRepository: ProblemTopicRepository,
    private val checkerRepository: ScriptCheckerRepository,
) {
    @Transactional
    fun createProblem(request: CreateProblemRequest): ProblemDto {
        val topic =
            if(request.topic == null) null
            else problemTopicRepository.findByName(request.topic)
                ?: throw EntityNotFoundException("Problem topic with name \"${request.topic}\" not found")

        val scriptChecker =
            if(request.defaultScriptCheckerId == null) null
            else checkerRepository.findById(request.defaultScriptCheckerId).getOrNull()
                ?: throw EntityNotFoundException("Script checker with id \"${request.defaultScriptCheckerId}\" not found")

        val createdProblem = problemRepository.save(Problem(
            title = request.title,
            topic = topic,
            difficulty = request.difficulty!!,
            statement = request.statement,
            executionTimeLimitMs = request.executionTimeLimitMs,
            executionMemoryLimitKb = request.executionMemoryLimitKb,
            defaultEvaluationType = request.defaultEvaluationType!!,
            defaultScriptChecker = scriptChecker,
            isPublic = request.isPublic
        ))
        return createdProblem.toProblemDto()
    }

    @Transactional
    fun updateProblem(problemId: Int, request: UpdateProblemRequest): ProblemDto {
        val problem = problemRepository.findById(problemId).getOrNull()
            ?: throw EntityNotFoundException("Problem with id $problemId not found")

        val topic =
            if(request.topic == null) null
            else problemTopicRepository.findByName(request.topic)
                ?: throw EntityNotFoundException("Problem topic with name \"${request.topic}\" not found")

        val scriptChecker =
            if(request.defaultScriptCheckerId == null) null
            else checkerRepository.findById(request.defaultScriptCheckerId).getOrNull()
                ?: throw EntityNotFoundException("Script checker with id \"${request.defaultScriptCheckerId}\" not found")

        request.title?.let { problem.title = it }
        topic?.let { problem.topic = it }
        request.difficulty?.let { problem.difficulty = it }
        request.statement?.let { problem.statement = it }
        request.executionTimeLimitMs?.let { problem.executionTimeLimitMs = it }
        request.executionMemoryLimitKb?.let { problem.executionMemoryLimitKb = it }
        request.defaultEvaluationType?.let { problem.defaultEvaluationType = it }
        scriptChecker?.let{ problem.defaultScriptChecker = it }
        request.isPublic?.let { problem.isPublic = it }

        val updatedProblem = problemRepository.save(problem)

        return updatedProblem.toProblemDto()
    }
    @Transactional
    fun deleteProblem(problemId: Int) {
        problemRepository.deleteById(problemId)
    }

    @Transactional(readOnly = true)
    fun getProblemTopics(): List<ProblemTopic> {
        val problemTopics = problemTopicRepository.findAll()
        return problemTopics
    }

    @Transactional(readOnly = true)
    fun getProblemDto(id: Int): ProblemDto {
        val problem = problemRepository.findById(id).getOrNull()
            ?: throw EntityNotFoundException("Problem with id: $id not found")
        return problem.toProblemDto()
    }

    @Transactional(readOnly = true)
    fun getProblemsWrapped(request: GetProblemsRequest, includePrivate: Boolean = false): ProblemPageResponse {
        // build specification for query
        val spec = Specification<Problem> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            
            // Only fetch public problems if includePrivate is false
            if (!includePrivate) {
                predicates.add(cb.equal(root.get<Boolean>("isPublic"), true))
            }

            // static filters
            request.topicName?.let {
                val topicJoin = root.join<Problem, ProblemTopic>("topic")
                predicates.add(cb.equal(topicJoin.get<String>("name"), it))
            }
            request.difficulty?.let {
                predicates.add(cb.equal(root.get<ProblemDifficulty>("difficulty"), it))
            }

            // keyset logic
            // (difficulty > lastDifficulty) OR (difficulty = lastDifficulty AND id > lastId)
            val decodedCursor = CursorUtil.decode<ProblemCursor>(request.cursor)
            if (decodedCursor != null) {
                val greaterDifficulty = cb.greaterThan(root.get("difficulty"), decodedCursor.lastSeenDifficulty)

                val equalDifficulty =
                    cb.equal(root.get<ProblemDifficulty>("difficulty"), decodedCursor.lastSeenDifficulty)
                val greaterId = cb.greaterThan(root.get<Int>("id"), decodedCursor.lastSeenId)
                val sameDifficultyNextId = cb.and(equalDifficulty, greaterId)

                predicates.add(cb.or(greaterDifficulty, sameDifficultyNextId))
            }

            cb.and(*predicates.toTypedArray())
        }

        // sorting and size (getting limit + 1 to know if there is a next page)
        val pageable = PageRequest.of(
            0, request.limit + 1,
            Sort.by("difficulty").ascending().and(Sort.by("id").ascending())
        )

        val fetchedProblems = problemRepository.findAll(spec, pageable).content

        val hasNext = fetchedProblems.size > request.limit
        val problems = if(hasNext) fetchedProblems.dropLast(1) else fetchedProblems

        val lastItem = problems.lastOrNull()
        val nextCursor = lastItem?.let { ProblemCursor(it.id!!, it.difficulty.ordinal) }
        val nextCursorToken = nextCursor?.let { CursorUtil.encode(it) }

        return ProblemPageResponse(
            problems.map { it.toProblemDto() },
            hasNext,
            nextCursorToken
        )
    }
}