package com.example.roomtracker.model

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UbicacionRow(
    @SerialName("id_usuario") val idUsuario: String,
    val latitud: Double,
    val longitud: Double,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class AmistadConPrivacidad(
    @SerialName("id_amistad")   val idAmistad: String,
    @SerialName("id_usuario_1") val idUsuario1: String,
    @SerialName("id_usuario_2") val idUsuario2: String,
    val estado: String,
    @SerialName("puede_ver_1")  val puedeVer1: Boolean = true,
    @SerialName("puede_ver_2")  val puedeVer2: Boolean = true
)

data class AmigoEnMapa(
    val idUsuario: String,
    val nombre: String,
    val apellido: String,
    val posicion: LatLng
)
