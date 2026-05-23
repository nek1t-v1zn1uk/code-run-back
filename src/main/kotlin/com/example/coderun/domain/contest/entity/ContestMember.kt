package com.example.coderun.domain.contest.entity

import com.example.coderun.domain.contest.dto.ContestMemberDto
import com.example.coderun.domain.users.User
import jakarta.persistence.*

@Entity
@Table(
    name = "contest_members",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["contest_id", "user_id"])
    ]
)
data class ContestMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    var contest: Contest,

    @Column(name = "result_points")
    var resultPoints: Int? = null,

    @Column(name = "result_place")
    var resultPlace: Int? = null
) {
    fun toDto() = ContestMemberDto(
        id = this.id!!,
        userId = this.user.id!!,
        contestId = this.contest.id!!,
        resultPoints = this.resultPoints,
        resultPlace = this.resultPlace
    )
}
