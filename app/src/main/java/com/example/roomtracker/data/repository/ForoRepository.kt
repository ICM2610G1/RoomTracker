package com.example.roomtracker.data.repository

import com.example.roomtracker.model.ForoMensajeRow
import com.example.roomtracker.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ForoRepository {

    suspend fun loadMensajes(institucion: String): List<ForoMensajeRow> =
        supabase.from("foro_mensaje").select {
            filter { eq("institucion", institucion) }
            order("fecha_publicacion", Order.DESCENDING)
            limit(100)
        }.decodeList()

    suspend fun getPerfilNombre(uid: String): Pair<String, String> {
        @Serializable
        data class PerfilRow(val nombre: String, val apellido: String)
        return try {
            val p = supabase.from("usuarios").select {
                filter { eq("id_usuario", uid) }
            }.decodeSingle<PerfilRow>()
            Pair(p.nombre, p.apellido)
        } catch (_: Exception) { Pair("Usuario", "") }
    }

    suspend fun publicar(
        uid: String,
        email: String,
        nombre: String,
        contenido: String,
        institucion: String
    ) {
        @Serializable
        data class ForoInsert(
            @SerialName("id_usuario") val idUsuario: String,
            val email: String,
            val nombre: String,
            val contenido: String,
            val institucion: String
        )
        supabase.from("foro_mensaje").insert(
            ForoInsert(uid, email, nombre, contenido.trim(), institucion)
        )
    }
}
