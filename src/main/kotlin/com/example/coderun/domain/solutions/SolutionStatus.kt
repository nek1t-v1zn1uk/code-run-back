package com.example.coderun.domain.solutions

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class SolutionStatus {
    IN_QUEUE,
    COMPILING,
    COMPILATION_ERROR,
    EXECUTION,
    RUNTIME_ERROR,
    SUCCESS,
    TEST_FAILED,
    TIME_LIMIT_EXCEEDED,
    MEMORY_LIMIT_EXCEEDED,
    OUTPUT_LIMIT_EXCEEDED,
    INTERNAL_ERROR;

    @Converter(autoApply = true)
    class SolutionStatusConverter : AttributeConverter<SolutionStatus, String> {
        override fun convertToDatabaseColumn(attribute: SolutionStatus?): String? {
            return attribute?.name?.lowercase()
        }

        override fun convertToEntityAttribute(dbData: String?): SolutionStatus? {
            return dbData?.let { SolutionStatus.valueOf(it.uppercase()) }
        }
    }
}
