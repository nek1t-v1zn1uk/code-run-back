package com.example.coderun.domain.tests.entity

import com.example.coderun.domain.solutions.AvailableLanguage
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
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