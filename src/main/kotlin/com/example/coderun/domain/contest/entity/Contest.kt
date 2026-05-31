package com.example.coderun.domain.contest.entity

import com.example.coderun.domain.contest.dto.ContestDto
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "contests")
data class Contest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column
    var name: String? = null,

    @Column
    var overview: String? = null,

    @Column
    var rules: String? = null,

    @Column(name = "start_time", nullable = false)
    var startTime: Instant,

    @Column(name = "freeze_time")
    var freezeTime: Instant? = null,

    @Column(name = "end_time", nullable = false)
    var endTime: Instant,

    @Column(name = "results_calculated", nullable = false)
    var resultsCalculated: Boolean = false
) {
    fun toDto() = ContestDto(
        id = this.id!!,
        name = this.name,
        overview = this.overview,
        rules = this.rules,
        startTime = this.startTime,
        freezeTime = this.freezeTime,
        endTime = this.endTime
    )
}
