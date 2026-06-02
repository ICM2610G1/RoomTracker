package com.example.roomtracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventoJson(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    @SerialName("imagen_url")     val imagenUrl: String     = "",
    @SerialName("fecha_inicio")   val fechaInicio: String,
    @SerialName("fecha_fin")      val fechaFin: String,
    val lugar: String,
    @SerialName("url_inscripcion") val urlInscripcion: String = "",
    val activo: Boolean = true,
    @SerialName("id_nodo") val idNodo: String? = null
)

@Serializable
data class MenuItemJson(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val precio: Int,
    @SerialName("imagen_url")     val imagenUrl: String     = "",
    val lugar: String                                        = "",
    @SerialName("tiempo_entrega") val tiempoEntrega: String  = "",
    val disponible: Boolean                                  = true,
    val calorias: Int                                        = 0
)

@Serializable
data class OportunidadJson(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val requisitos: String                                    = "",
    @SerialName("fecha_publicacion") val fechaPublicacion: String = "",
    @SerialName("fecha_cierre")      val fechaCierre: String      = "",
    @SerialName("url_inscripcion")   val urlInscripcion: String   = "",
    val disponible: Boolean = true,
    val lugar: String       = "",
    @SerialName("id_nodo") val idNodo: String? = null
)

@Serializable
data class ProductoJson(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val precio: Int,
    val imagen_url: String       = "",
    val disponible: Boolean      = true,
    val tallas: List<String>     = emptyList()
)
