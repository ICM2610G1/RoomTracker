package com.example.roomtracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaStats(
    val fecha: String,
    val pasos: Int,
    @SerialName("distancia_metros") val distanciaMetros: Double
)

@Serializable
data class DistanciaJson(
    @SerialName("id_usuario")              val idUsuario: String,
    @SerialName("total_pasos")             val totalPasos: Int,
    @SerialName("total_distancia_metros")  val totalDistanciaMetros: Double,
    val historial: List<DiaStats> = emptyList()
)
