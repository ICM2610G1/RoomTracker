package com.example.roomtracker.data.repository

import com.example.roomtracker.model.EventoJson
import com.example.roomtracker.model.MenuItemJson
import com.example.roomtracker.model.OportunidadJson
import com.example.roomtracker.model.ProductoJson
import com.example.roomtracker.supabase
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json

class ContentRepository {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadEventos(carpeta: String): List<EventoJson> {
        val bytes = supabase.storage["eventos"].downloadPublic("$carpeta/eventos.json")
        return json.decodeFromString(String(bytes))
    }

    suspend fun loadMenu(carpeta: String): List<MenuItemJson> {
        val bytes = supabase.storage["comida"].downloadPublic("$carpeta/menu.json")
        return json.decodeFromString(String(bytes))
    }

    suspend fun loadOportunidades(carpeta: String): List<OportunidadJson> {
        val bytes = supabase.storage["oportunidades"].downloadPublic("$carpeta/oportunidades.json")
        return json.decodeFromString(String(bytes))
    }

    suspend fun loadProductos(carpeta: String): List<ProductoJson> {
        val bytes = supabase.storage["tienda"].downloadPublic("$carpeta/productos.json")
        return json.decodeFromString(String(bytes))
    }
}
