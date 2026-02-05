package com.example.coderun.domain.problems

import jakarta.persistence.*

@Entity
@Table(name = "available_languages", uniqueConstraints = [UniqueConstraint(columnNames = ["language", "version"])])
data class AvailableLanguage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false)
    var language: String,

    @Column(nullable = false)
    var version: String
)
