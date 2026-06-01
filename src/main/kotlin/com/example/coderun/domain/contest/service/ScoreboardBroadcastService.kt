package com.example.coderun.domain.contest.service

import com.example.coderun.domain.contest.event.SolutionEvaluatedEvent
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class ScoreboardBroadcastService(
    private val contestService: ContestService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @EventListener
    fun handleSolutionEvaluatedEvent(event: SolutionEvaluatedEvent) {
        val scoreboard = contestService.getScoreboard(event.contestId)
        messagingTemplate.convertAndSend("/topic/contests/${event.contestId}/scoreboard", scoreboard)
    }
}
