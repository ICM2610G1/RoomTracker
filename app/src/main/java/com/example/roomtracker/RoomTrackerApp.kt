package com.example.roomtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.example.roomtracker.service.FCMService
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class RoomTrackerApp : Application() {

    companion object {
        lateinit var appContext: Application
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        createNotificationChannel()
        syncFCMToken()
    }

    // ── Canal de notificaciones (requerido en Android 8+) ─────────────────────
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCMService.CHANNEL_ID,
                "RoomTracker",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de amigos y mensajes"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // ── Sincroniza el token FCM actual con Supabase al arrancar ───────────────
    // (por si el token se generó antes de que el usuario iniciara sesión)
    private fun syncFCMToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val uid = supabase.auth.currentUserOrNull()?.id ?: return@launch

                    @Serializable
                    data class TokenUpdate(@SerialName("fcm_token") val fcmToken: String)

                    supabase.from("usuarios").update(TokenUpdate(token)) {
                        filter { eq("id_usuario", uid) }
                    }
                    Log.d("FCM", "Token sincronizado al arrancar para uid=$uid")
                } catch (e: Exception) {
                    Log.e("FCM", "Error sincronizando token: ${e.message}")
                }
            }
        }
    }
}
