package com.example.roomtracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AmistadRow(
    @SerialName("id_amistad")     val idAmistad: String,
    @SerialName("id_usuario_1")   val idUsuario1: String,
    @SerialName("id_usuario_2")   val idUsuario2: String,
    val estado: String,
    @SerialName("fecha_creacion") val fechaCreacion: String? = null,
    @SerialName("puede_ver_1")    val puedeVer1: Boolean?   = null,
    @SerialName("puede_ver_2")    val puedeVer2: Boolean?   = null
)

data class AmigoConAmistad(
    val amistadId: String,
    val usuario: UsuarioRow,
    val esUsuario1: Boolean      = true,
    val meEstoyMostrando: Boolean = true
)
