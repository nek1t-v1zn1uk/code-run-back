package com.example.coderun.domain.tests.service

import com.example.coderun.domain.solutions.service.AvailableLanguageService
import com.example.coderun.domain.tests.dto.CreateScriptCheckerRequest
import com.example.coderun.domain.tests.dto.ScriptCheckerDto
import com.example.coderun.domain.tests.dto.UpdateScriptCheckerRequest
import com.example.coderun.domain.tests.entity.ScriptChecker
import com.example.coderun.domain.tests.repository.ScriptCheckerRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class ScriptCheckerService(
    private val checkerRepository: ScriptCheckerRepository,
    private val languageService: AvailableLanguageService,
) {
    @Transactional
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

    @Transactional
    fun updateScriptChecker(scriptCheckerId: Int, request: UpdateScriptCheckerRequest): ScriptCheckerDto {
        val scriptChecker = checkerRepository.findById(scriptCheckerId).getOrNull()
            ?: throw EntityNotFoundException("Script checker with id '$scriptCheckerId' not found")

        request.name?.let { scriptChecker.name = it }
        request.language?.let {
            scriptChecker.language = languageService.getLanguage(it, request.languageVersion)
        }
        request.code?.let { scriptChecker.code = it }

        val newScriptChecker = checkerRepository.save(scriptChecker)

        return newScriptChecker.toDto()
    }

    @Transactional
    fun deleteScriptChecker(scriptCheckerId: Int) {
        checkerRepository.deleteById(scriptCheckerId)
    }

    fun getScriptCheckerDto(scriptCheckerId: Int): ScriptCheckerDto {
        val scriptChecker = checkerRepository.findById(scriptCheckerId).getOrNull()
            ?: throw EntityNotFoundException("Script checker with id '$scriptCheckerId' not found")
        return scriptChecker.toDto()
    }
}