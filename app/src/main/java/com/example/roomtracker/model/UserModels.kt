package com.example.roomtracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Fila de la tabla `usuarios` en Supabase */
@Serializable
data class UsuarioRow(
    @SerialName("id_usuario")    val idUsuario: String,
    val nombre: String,
    val apellido: String,
    val tipo: String,
    val estado: String,
    @SerialName("id_institucion") val idInstitucion: String? = null,
    @SerialName("foto_url")       val fotoUrl: String?       = null,
    @SerialName("session_token")  val sessionToken: String?  = null,
    @SerialName("fcm_token")      val fcmToken: String?      = null
)

/** Perfil que se muestra en la pantalla de ajustes */
data class UserProfile(
    val nombre: String,
    val apellido: String,
    val email: String,
    val fotoUrl: String? = null
)
