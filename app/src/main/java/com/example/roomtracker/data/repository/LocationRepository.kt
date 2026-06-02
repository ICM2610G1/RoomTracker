package com.example.roomtracker.data.repository

import com.example.roomtracker.model.AmistadConPrivacidad
import com.example.roomtracker.model.UbicacionRow
import com.example.roomtracker.model.UsuarioRow
import com.example.roomtracker.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class LocationRepository {

    suspend fun upsertLocation(uid: String, lat: Double, lng: Double) {
        @Serializable
        data class UbicacionUpsert(
            @SerialName("id_usuario") val idUsuario: String,
            val latitud: Double,
            val longitud: Double
        )
        supabase.from("ubicacion_usuario").upsert(
            UbicacionUpsert(uid, lat, lng)
        ) { onConflict = "id_usuario" }
    }

    suspend fun deleteLocation(uid: String) {
        supabase.from("ubicacion_usuario").delete {
            filter { eq("id_usuario", uid) }
        }
    }

    suspend fun loadFriendships(): List<AmistadConPrivacidad> =
        supabase.from("amistad").select {
            filter { eq("estado", "aceptada") }
        }.decodeList()

    suspend fun loadUserLocation(userId: String): UbicacionRow? =
        supabase.from("ubicacion_usuario").select {
            filter { eq("id_usuario", userId) }
        }.decodeList<UbicacionRow>().firstOrNull()

    suspend fun loadUser(userId: String): UsuarioRow? =
        supabase.from("usuarios").select {
            filter { eq("id_usuario", userId) }
        }.decodeList<UsuarioRow>().firstOrNull()

    suspend fun getSessionToken(uid: String): String? = try {
        supabase.from("usuarios").select {
            filter { eq("id_usuario", uid) }
        }.decodeSingle<UsuarioRow>().sessionToken
    } catch (_: Exception) { null }

    suspend fun togglePrivacidad(amistadId: String, campo: String, valor: Boolean) {
        supabase.from("amistad").update(mapOf(campo to valor)) {
            filter { eq("id_amistad", amistadId) }
        }
    }
}
