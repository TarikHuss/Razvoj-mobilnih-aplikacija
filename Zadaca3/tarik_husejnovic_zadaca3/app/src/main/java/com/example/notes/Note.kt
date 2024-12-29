package com.example.notes

class Note (
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String,
    var text: String,
    var date: String
)
