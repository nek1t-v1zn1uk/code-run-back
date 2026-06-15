package com.example.coderun.domain.problems.service

import com.example.coderun.domain.problems.dto.CreateProblemTopicRequest
import com.example.coderun.domain.problems.dto.UpdateProblemTopicRequest
import com.example.coderun.domain.problems.entity.ProblemTopic
import com.example.coderun.domain.problems.repository.ProblemTopicRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class ProblemTopicService(
    private val problemTopicRepository: ProblemTopicRepository
) {
    @Transactional
    fun createTopic(request: CreateProblemTopicRequest): ProblemTopic {
        val existing = problemTopicRepository.findByName(request.name)
        if (existing != null) {
            throw IllegalArgumentException("Topic with name '${request.name}' already exists.")
        }
        return problemTopicRepository.save(ProblemTopic(name = request.name))
    }

    @Transactional
    fun updateTopic(id: Int, request: UpdateProblemTopicRequest): ProblemTopic {
        val topic = problemTopicRepository.findById(id).getOrNull()
            ?: throw EntityNotFoundException("Topic with id $id not found")
        
        val existing = problemTopicRepository.findByName(request.name)
        if (existing != null && existing.id != id) {
            throw IllegalArgumentException("Topic with name '${request.name}' already exists.")
        }

        topic.name = request.name
        return problemTopicRepository.save(topic)
    }

    @Transactional
    fun deleteTopic(id: Int) {
        val topic = problemTopicRepository.findById(id).getOrNull()
            ?: throw EntityNotFoundException("Topic with id $id not found")
        problemTopicRepository.delete(topic)
    }
}
