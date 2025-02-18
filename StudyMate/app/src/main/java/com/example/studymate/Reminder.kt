package com.example.studymate.model

data class Reminder(
    val id: String = "",
    val title: String = "",
    val dueDate: String = "",
    val userId: String = ""
) {
    constructor() : this("", "", "", "")  // Potrebno za Firestore
}