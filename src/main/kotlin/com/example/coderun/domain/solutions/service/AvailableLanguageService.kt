package com.example.coderun.domain.solutions.service

import com.example.coderun.domain.solutions.entity.AvailableLanguage
import com.example.coderun.domain.solutions.repository.AvailableLanguageRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class AvailableLanguageService(
    private val languageRepository: AvailableLanguageRepository,
) {
    fun getLanguage(languageName: String, languageVersion: String? = null): AvailableLanguage {
        val language =
            when(languageRepository.countByLanguage(languageName)) {
                0 -> throw EntityNotFoundException("Language '$languageName' not found")
                1 -> languageRepository.findByLanguage(languageName)!!
                else ->
                    languageVersion?.let{
                        languageRepository.findByLanguageAndVersion(languageName, languageVersion)
                            ?: throw EntityNotFoundException("Language '$languageName' with version '$languageVersion' not found")
                    } ?: throw IllegalArgumentException("Language '$languageName' must have specified version")
            }

        return language
    }
}