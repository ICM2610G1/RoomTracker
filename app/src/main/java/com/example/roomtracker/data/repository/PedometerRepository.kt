package com.example.roomtracker.data.repository

import com.example.roomtracker.model.DistanciaJson
import com.example.roomtracker.supabase
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class PedometerRepository {

    private val json    = Json { ignoreUnknownKeys = true }
    private val bucket  = "distancias"
    private val folder  = "javeriana"

    suspend fun loadStats(uid: String): DistanciaJson? = try {
        val bytes = supabase.storage[bucket].downloadPublic("$folder/$uid.json")
        json.decodeFromString(String(bytes))
    } catch (_: Exception) { null }

    suspend fun saveStats(uid: String, stats: DistanciaJson) {
        val bytes = Json.encodeToString(stats).toByteArray()
        try { supabase.storage[bucket].delete(listOf("$folder/$uid.json")) } catch (_: Exception) {}
        supabase.storage[bucket].upload("$folder/$uid.json", bytes)
    }
}
