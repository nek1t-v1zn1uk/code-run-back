package com.example.coderun.exception

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    // Handle Custom Business Exceptions
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<ApiErrorResponse> {
        val error = ApiErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(EntityExistsException::class)
    fun handleEntityExists(ex: EntityExistsException): ResponseEntity<ApiErrorResponse> {
        val error = ApiErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message
        )
        return ResponseEntity(error, HttpStatus.CONFLICT)
    }

    // Handle Request Validation Errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate {
            it.field.camelToSnakeCase() to (it.defaultMessage ?: "Invalid value")
        }

        val error = ApiErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Validation failed",
            errors = fieldErrors
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    // Handle General Exceptions (The "Everything Else" catch)
    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ApiErrorResponse> {
        val error = ApiErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An unexpected error occurred"
        )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    fun String.camelToSnakeCase(): String {
        return this.replace("([a-z])([A-Z]+)".toRegex(), "$1_$2").lowercase()
    }
}