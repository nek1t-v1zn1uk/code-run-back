package com.example.coderun.domain.contest.event

import org.springframework.context.ApplicationEvent

class SolutionEvaluatedEvent(
    source: Any,
    val contestId: Int
) : ApplicationEvent(source)
