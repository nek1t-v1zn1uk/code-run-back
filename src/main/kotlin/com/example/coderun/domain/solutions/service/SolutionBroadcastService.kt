package com.example.coderun.domain.solutions.service

import com.example.coderun.domain.solutions.dto.SolutionDto
import com.example.coderun.domain.solutions.entity.Solution
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class SolutionBroadcastService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    fun broadcastSolutionUpdate(solution: Solution) {
        val destination = "/topic/solutions/${solution.id}"
        val dto = solution.toDto()
        messagingTemplate.convertAndSend(destination, dto)
    }
}
