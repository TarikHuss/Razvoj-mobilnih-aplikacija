package com.example.studymate.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = ""
) {
    constructor() : this("", "", "")
}