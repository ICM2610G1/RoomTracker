package com.example.roomtracker.data.repository

import android.app.Application
import android.net.Uri
import com.example.roomtracker.model.ChatRow
import com.example.roomtracker.model.MensajeRow
import com.example.roomtracker.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

class ChatRepository(private val application: Application) {

    suspend fun loadChatsDeServicio(uid: String): List<ChatRow> =
        supabase.from("chat").select {
            filter {
                eq("id_usuario", uid)
                eq("contexto_tipo", "servicio")
            }
        }.decodeList()

    suspend fun loadChatsPorOperador(operadorId: String): List<ChatRow> =
        supabase.from("chat").select {
            filter { eq("contexto_id", operadorId) }
        }.decodeList()

    suspend fun findChat(uid: String, operadorId: String): ChatRow? =
        supabase.from("chat").select {
            filter {
                eq("id_usuario", uid)
                eq("contexto_tipo", "servicio")
                eq("contexto_id", operadorId)
            }
        }.decodeList<ChatRow>().firstOrNull()

    suspend fun createChat(uid: String, operadorId: String): String {
        @Serializable
        data class ChatInsert(
            @SerialName("id_chat")       val idChat: String,
            @SerialName("id_usuario")    val idUsuario: String,
            @SerialName("contexto_tipo") val contextoTipo: String,
            @SerialName("contexto_id")   val contextoId: String
        )
        val newId = UUID.randomUUID().toString()
        supabase.from("chat").insert(
            ChatInsert(newId, uid, "servicio", operadorId)
        )
        return newId
    }

    suspend fun loadMessages(chatId: String): List<MensajeRow> =
        supabase.from("mensaje").select {
            filter { eq("id_chat", chatId) }
        }.decodeList()

    suspend fun loadLastMessage(chatId: String): MensajeRow? =
        supabase.from("mensaje").select {
            filter { eq("id_chat", chatId) }
            order("fecha_envio", Order.DESCENDING)
            limit(1)
        }.decodeList<MensajeRow>().firstOrNull()

    suspend fun sendTextMessage(chatId: String, uid: String, text: String) {
        @Serializable
        data class MensajeInsert(
            @SerialName("id_mensaje")     val idMensaje: String,
            @SerialName("id_chat")        val idChat: String,
            @SerialName("id_emisor")      val idEmisor: String,
            val tipo: String,
            @SerialName("contenido_texto") val contenidoTexto: String
        )
        supabase.from("mensaje").insert(
            MensajeInsert(UUID.randomUUID().toString(), chatId, uid, "texto", text)
        )
    }

    suspend fun sendImageMessage(chatId: String, uid: String, imageUri: Uri): String {
        val bytes = application.contentResolver
            .openInputStream(imageUri)?.use { it.readBytes() }
            ?: throw Exception("No se pudo leer la imagen")
        val mimeType = application.contentResolver.getType(imageUri) ?: "image/jpeg"
        val ext = mimeType.substringAfterLast("/").replace("jpeg", "jpg").take(4)
        val path = "javeriana/$chatId/${UUID.randomUUID()}.$ext"

        supabase.storage["chat-media"].upload(path, bytes)
        val imageUrl = supabase.storage["chat-media"].publicUrl(path)

        @Serializable
        data class MensajeInsert(
            @SerialName("id_mensaje")      val idMensaje: String,
            @SerialName("id_chat")         val idChat: String,
            @SerialName("id_emisor")       val idEmisor: String,
            val tipo: String,
            @SerialName("contenido_texto") val contenidoTexto: String
        )
        supabase.from("mensaje").insert(
            MensajeInsert(UUID.randomUUID().toString(), chatId, uid, "imagen", imageUrl)
        )
        return imageUrl
    }
}
