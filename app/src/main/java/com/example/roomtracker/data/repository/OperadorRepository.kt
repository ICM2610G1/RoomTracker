package com.example.roomtracker.data.repository

import com.example.roomtracker.model.ChatRow
import com.example.roomtracker.model.ConversacionEstudiante
import com.example.roomtracker.model.MensajeRow
import com.example.roomtracker.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class OperadorRepository {

    suspend fun loadConversaciones(operadorId: String): List<ConversacionEstudiante> {
        val chats = supabase.from("chat").select {
            filter { eq("contexto_id", operadorId) }
        }.decodeList<ChatRow>()

        return chats.mapNotNull { chat ->
            try {
                @Serializable
                data class PerfilRow(
                    @SerialName("id_usuario") val id: String,
                    val nombre: String,
                    val apellido: String
                )
                val perfil = supabase.from("usuarios").select {
                    filter { eq("id_usuario", chat.idUsuario) }
                }.decodeSingle<PerfilRow>()

                val ultimoMensaje = supabase.from("mensaje").select {
                    filter { eq("id_chat", chat.idChat) }
                    order("fecha_envio", Order.DESCENDING)
                    limit(1)
                }.decodeList<MensajeRow>().firstOrNull()

                ConversacionEstudiante(
                    idChat           = chat.idChat,
                    idEstudiante     = chat.idUsuario,
                    nombreEstudiante = "${perfil.nombre} ${perfil.apellido}".trim(),
                    lastMessage      = ultimoMensaje?.contenidoTexto ?: "Sin mensajes aún",
                    lastTime         = ultimoMensaje?.fechaEnvio ?: ""
                )
            } catch (_: Exception) { null }
        }
    }
}
