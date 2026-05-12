package com.example.coderun.domain.tests.service

import com.example.coderun.domain.solutions.service.AvailableLanguageService
import com.example.coderun.domain.tests.dto.CreateScriptCheckerRequest
import com.example.coderun.domain.tests.dto.ScriptCheckerDto
import com.example.coderun.domain.tests.entity.ScriptChecker
import com.example.coderun.domain.tests.repository.ScriptCheckerRepository
import org.springframework.stereotype.Service

@Service
class ScriptCheckerService(
    private val checkerRepository: ScriptCheckerRepository,
    private val languageService: AvailableLanguageService,
) {
    fun createScriptChecker(request: CreateScriptCheckerRequest): ScriptCheckerDto {
        val language = languageService.getLanguage(request.language, request.languageVersion)

        val newChecker = checkerRepository.save(
            ScriptChecker(
                name = request.name,
                language = language,
                code = request.code
            )
        )

        return newChecker.toDto()
    }
}