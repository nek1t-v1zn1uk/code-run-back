package com.example.coderun.domain.problems.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class EvaluationType {
    EXACT_MATCH,
    SCRIPT_CHECK;

    @Converter(autoApply = true)
    class EvaluationTypeConverter : AttributeConverter<EvaluationType, String> {
        override fun convertToDatabaseColumn(attribute: EvaluationType?): String? {
            return attribute?.name?.lowercase()
        }

        override fun convertToEntityAttribute(dbData: String?): EvaluationType? {
            return dbData?.let { valueOf(it.uppercase()) }
        }
    }
}
