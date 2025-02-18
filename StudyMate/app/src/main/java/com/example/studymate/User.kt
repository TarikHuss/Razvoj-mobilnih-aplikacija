package com.example.studymate.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = ""
) {
    // Prazan konstruktor koji je potreban za Firestore
    constructor() : this("", "", "")
}