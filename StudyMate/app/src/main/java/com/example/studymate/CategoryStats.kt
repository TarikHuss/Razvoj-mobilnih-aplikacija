package com.example.studymate.model

data class CategoryStats(
    val category: String,
    val quizzesTaken: Int,
    val averageScore: Float,
    val bestScore: Int
)