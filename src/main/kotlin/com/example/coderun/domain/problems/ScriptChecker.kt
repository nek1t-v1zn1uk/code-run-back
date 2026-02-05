package com.example.coderun.domain.problems

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "script_checkers")
data class ScriptChecker(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    var language: AvailableLanguage,

    @Column(nullable = false)
    var code: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
