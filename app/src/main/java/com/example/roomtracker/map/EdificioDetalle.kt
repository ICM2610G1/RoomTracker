package com.example.roomtracker.map

data class EspacioDetalle(
    val codigo: String,
    val nombre: String,
    val tipo: String   // "laboratorio" | "aula" | "coworking" | "oficina" | "servicio" | "otro"
)

data class PisoDetalle(
    val numero: String,              // "S1", "P01", "P08", "P14"
    val nombre: String,
    val espacios: List<EspacioDetalle>,
    val servicios: List<String> = emptyList()
)

data class EdificioDetalle(
    val nombre: String,
    val descripcion: String,
    val imagenUrl: String?,
    val pisos: List<PisoDetalle>
)
