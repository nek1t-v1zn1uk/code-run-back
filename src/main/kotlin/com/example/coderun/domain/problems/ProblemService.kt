package com.example.coderun.domain.problems

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class ProblemService (
    private val problemRepository: ProblemRepository,
    private val problemTopicRepository: ProblemTopicRepository,
) {
    fun getProblemTopics(): List<ProblemTopic> {
        val problemTopics = problemTopicRepository.findAll()
        return problemTopics
    }
    fun getProblemDto(id: Int): ProblemDto {
        val problem = problemRepository.findById(id).getOrNull()
            ?: throw EntityNotFoundException("Problem with id: $id not found")
        return problem.toProblemDto()
    }
    fun getProblemsDto(): List<ProblemDto> {
        val problems = problemRepository.findAll()
        return problems.map { it.toProblemDto() }
    }
}