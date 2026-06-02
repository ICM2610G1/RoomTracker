package com.example.roomtracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForoMensajeRow(
    val id: String,
    @SerialName("id_usuario")         val idUsuario: String,
    val email: String,
    val nombre: String,
    val contenido: String,
    @SerialName("fecha_publicacion")  val fechaPublicacion: String,
    val institucion: String
)

data class ForoMensajeUi(
    val id: String,
    val email: String,
    val nombre: String,
    val contenido: String,
    val fechaFormateada: String,
    val esMio: Boolean
)
