package com.example.roomtracker

import android.content.Context
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SharedPrefsSessionManager(context: Context) : SessionManager {

    private val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun loadSession(): UserSession? {
        val str = prefs.getString("session", null) ?: return null
        return try {
            json.decodeFromString<UserSession>(str)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveSession(session: UserSession) {
        prefs.edit().putString("session", json.encodeToString(session)).apply()
    }

    override suspend fun deleteSession() {
        prefs.edit().remove("session").apply()
    }
}
