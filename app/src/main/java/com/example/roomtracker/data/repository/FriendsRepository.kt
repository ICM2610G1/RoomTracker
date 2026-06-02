package com.example.roomtracker.data.repository

import com.example.roomtracker.model.AmistadRow
import com.example.roomtracker.model.UsuarioRow
import com.example.roomtracker.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

class FriendsRepository {

    suspend fun loadFriendships(estado: String = "aceptada"): List<AmistadRow> =
        supabase.from("amistad").select {
            filter { eq("estado", estado) }
        }.decodeList()

    suspend fun loadPendingReceived(uid: String): List<AmistadRow> =
        supabase.from("amistad").select {
            filter {
                eq("id_usuario_2", uid)
                eq("estado", "pendiente")
            }
        }.decodeList()

    suspend fun loadPendingSent(uid: String): List<AmistadRow> =
        supabase.from("amistad").select {
            filter {
                eq("id_usuario_1", uid)
                eq("estado", "pendiente")
            }
        }.decodeList()

    suspend fun loadUser(userId: String): UsuarioRow =
        supabase.from("usuarios").select {
            filter { eq("id_usuario", userId) }
        }.decodeSingle()

    suspend fun loadAllUsers(): List<UsuarioRow> =
        supabase.from("usuarios").select().decodeList()

    suspend fun getUserInstitucion(uid: String): String? = try {
        loadUser(uid).idInstitucion
    } catch (_: Exception) { null }

    suspend fun sendFriendRequest(uid: String, targetId: String) {
        @Serializable
        data class AmistadInsert(
            @SerialName("id_amistad")   val idAmistad: String,
            @SerialName("id_usuario_1") val idUsuario1: String,
            @SerialName("id_usuario_2") val idUsuario2: String,
            val estado: String
        )
        supabase.from("amistad").insert(
            AmistadInsert(UUID.randomUUID().toString(), uid, targetId, "pendiente")
        )
    }

    suspend fun acceptRequest(amistadId: String) {
        @Serializable data class EstadoUpdate(val estado: String)
        supabase.from("amistad").update(EstadoUpdate("aceptada")) {
            filter { eq("id_amistad", amistadId) }
        }
    }

    suspend fun deleteAmistad(amistadId: String) {
        supabase.from("amistad").delete {
            filter { eq("id_amistad", amistadId) }
        }
    }
}
