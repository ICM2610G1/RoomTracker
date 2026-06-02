package com.example.roomtracker.data.repository

import com.example.roomtracker.model.CarnetData
import com.example.roomtracker.model.CarnetRow
import com.example.roomtracker.model.EstadisticasJson
import com.example.roomtracker.model.UsuarioRow
import com.example.roomtracker.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AcademicRepository {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadEstadisticas(uid: String, carpeta: String): EstadisticasJson {
        val bytes = supabase.storage["notas"].downloadPublic("$carpeta/$uid.json")
        return json.decodeFromString(String(bytes))
    }

    suspend fun saveEstadisticas(uid: String, carpeta: String, stats: EstadisticasJson) {
        val path  = "$carpeta/$uid.json"
        val bytes = json.encodeToString(stats).toByteArray()
        try { supabase.storage["notas"].delete(listOf(path)) } catch (_: Exception) {}
        supabase.storage["notas"].upload(path, bytes)
    }

    suspend fun loadCarnet(uid: String): CarnetData {
        val carnet = supabase.from("carnet").select {
            filter { eq("id_usuario", uid) }
        }.decodeList<CarnetRow>().firstOrNull()
            ?: throw Exception("No se encontró tu carnet en el sistema")

        val usuario = supabase.from("usuarios").select {
            filter { eq("id_usuario", uid) }
        }.decodeList<UsuarioRow>().firstOrNull()
            ?: throw Exception("No se encontró tu perfil de usuario")

        return CarnetData(
            nombre           = usuario.nombre.uppercase(),
            apellido         = usuario.apellido.uppercase(),
            tipo             = usuario.tipo.uppercase(),
            codigoEstudiante = carnet.codigoEstudiante,
            programa         = carnet.programa,
            facultad         = carnet.facultad,
            fechaVencimiento = carnet.fechaVencimiento ?: "",
            codigoQr         = carnet.codigoQr ?: "",
            fotoUrl          = usuario.fotoUrl
        )
    }
}
