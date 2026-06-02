package com.example.roomtracker.model

data class ConversacionEstudiante(
    val idChat: String,
    val idEstudiante: String,
    val nombreEstudiante: String,
    val lastMessage: String,
    val lastTime: String
)
