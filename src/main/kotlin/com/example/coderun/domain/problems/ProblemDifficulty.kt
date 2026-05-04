package com.example.coderun.domain.problems

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class ProblemDifficulty {
    VERY_EASY,
    EASY,
    MEDIUM,
    HARD,
    VERY_HARD;

    @Converter(autoApply = true)
    class ProblemDifficultyConverter : AttributeConverter<ProblemDifficulty, String> {
        override fun convertToDatabaseColumn(attribute: ProblemDifficulty?): String? {
            return attribute?.name?.lowercase()
        }

        override fun convertToEntityAttribute(dbData: String?): ProblemDifficulty? {
            return dbData?.let { ProblemDifficulty.valueOf(it.uppercase()) }
        }
    }
}
