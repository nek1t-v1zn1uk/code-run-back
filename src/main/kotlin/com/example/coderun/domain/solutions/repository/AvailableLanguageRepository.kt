package com.example.coderun.domain.solutions.repository

import com.example.coderun.domain.solutions.entity.AvailableLanguage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AvailableLanguageRepository : JpaRepository<AvailableLanguage, Int> {
    fun findByLanguageAndVersion(language: String, version: String): AvailableLanguage?
    fun countByLanguage(language: String): Int
    fun findByLanguage(language: String): AvailableLanguage?
}