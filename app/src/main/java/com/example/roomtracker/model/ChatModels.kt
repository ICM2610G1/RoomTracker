package com.example.roomtracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Operadores de servicio ───────────────────────────────────────────────────

data class OperadorInfo(
    val idUsuario: String,
    val nombre: String,
    val apellido: String
) {
    val nombreCompleto get() = "$nombre $apellido"
}

object OperadoresServicio {
    const val SOPORTE_ID      = "de8f11be-e650-4dcc-954c-53571754ab78"
    const val ALIMENTICIOS_ID = "9ec87523-c833-4966-88b5-2476d0f53e78"
    const val PSICOLOGIA_ID   = "6b177a4d-aec2-4b26-b0e2-5526170936ae"

    val lista = listOf(
        OperadorInfo(SOPORTE_ID,      "Soporte Técnico", "Campus"),
        OperadorInfo(ALIMENTICIOS_ID, "Servicios",       "Alimenticios"),
        OperadorInfo(PSICOLOGIA_ID,   "Psicología",      "Estudiantil")
    )

    fun nombrePorId(id: String): String? =
        lista.firstOrNull { it.idUsuario == id }?.nombreCompleto
}

// ─── Filas de base de datos ───────────────────────────────────────────────────

@Serializable
data class ChatRow(
    @SerialName("id_chat")       val idChat: String,
    @SerialName("id_usuario")    val idUsuario: String,
    @SerialName("id_sesion")     val idSesion: String? = null,
    @SerialName("contexto_tipo") val contextoTipo: String,
    @SerialName("contexto_id")   val contextoId: String? = null
)

@Serializable
data class MensajeRow(
    @SerialName("id_mensaje")     val idMensaje: String,
    @SerialName("id_chat")        val idChat: String,
    @SerialName("id_emisor")      val idEmisor: String,
    val tipo: String,
    @SerialName("contenido_texto") val contenidoTexto: String? = null,
    @SerialName("fecha_envio")    val fechaEnvio: String? = null
)

// ─── Modelos de UI ────────────────────────────────────────────────────────────

data class ConversacionServicio(
    val operador: OperadorInfo,
    val idChat: String?,
    val lastMessage: String,
    val lastTime: String,
    val unread: Boolean
)

data class MensajeItem(
    val idMensaje: String,
    val contenidoTexto: String,
    val tipo: String = "texto",
    val esMio: Boolean,
    val fechaEnvio: String
)
