package com.example.roomtracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Institucion(
    @SerialName("id_institucion") val id: String,
    val nombre: String,
    @SerialName("dominio_correo") val dominioCorreo: String? = null,
    @SerialName("es_privada") val esPrivada: Boolean = false,
    @SerialName("carpeta_mapas") val carpetaMapas: String? = null
)
