package com.example.coderun.domain.solutions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

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