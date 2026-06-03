package com.example.coderun.domain.contest.service

import com.example.coderun.domain.contest.repository.ContestMemberRepository
import com.example.coderun.domain.contest.repository.ContestRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ContestResultScheduler(
    private val contestRepository: ContestRepository,
    private val contestMemberRepository: ContestMemberRepository,
    private val contestService: ContestService,
    private val cacheManager: CacheManager
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 60000)
    @Transactional
    fun calculateResultsForEndedContests() {
        val endedContests = contestRepository.findByEndTimeBeforeAndResultsCalculatedFalse(Instant.now())
        
        for (contest in endedContests) {
            log.info("Calculating final results for contest: ${contest.id} - ${contest.name}")
            try {
                // Evict cached scoreboard to ensure final results use fresh data
                cacheManager.getCache("scoreboard")?.evict(contest.id!!)
                val scoreboard = contestService.getScoreboard(contest.id!!)
                
                for (row in scoreboard.rows) {
                    val member = contestMemberRepository.findByContestIdAndUserId(contest.id!!, row.userId)
                    if (member != null) {
                        member.resultPoints = row.totalScore
                        member.resultPlace = row.place
                        contestMemberRepository.save(member)
                    }
                }
                
                contest.resultsCalculated = true
                contestRepository.save(contest)
                log.info("Successfully calculated results for contest: ${contest.id}")
            } catch (e: Exception) {
                log.error("Failed to calculate results for contest: ${contest.id}", e)
            }
        }
    }
}
