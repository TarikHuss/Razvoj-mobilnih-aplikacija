package com.example.studymate.model

data class QuizQuestion(
    val question: String,
    val correctAnswer: String,
    val options: List<String>,
    val questionType: QuestionType
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    WRITTEN
}