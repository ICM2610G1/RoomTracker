package com.example.roomtracker.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.roomtracker.MainActivity
import com.example.roomtracker.R
import com.example.roomtracker.supabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FCMService : FirebaseMessagingService() {

    // ── Cuando Firebase genera o renueva el token del dispositivo ─────────────
    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token renovado: $token")
        saveTokenToSupabase(token)
    }

    // ── Cuando llega una notificación con la app en primer plano ──────────────
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Mensaje recibido de: ${message.from}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "RoomTracker"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        showNotification(title, body, message.data)
    }

    // ── Guarda el token en la tabla usuarios ──────────────────────────────────
    private fun saveTokenToSupabase(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = supabase.auth.currentUserOrNull()?.id ?: run {
                    Log.d(TAG, "Sin sesión activa, token no guardado aún")
                    return@launch
                }

                @Serializable
                data class TokenUpdate(@SerialName("fcm_token") val fcmToken: String)

                supabase.from("usuarios").update(TokenUpdate(token)) {
                    filter { eq("id_usuario", uid) }
                }
                Log.d(TAG, "Token guardado para uid=$uid")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando token: ${e.message}")
            }
        }
    }

    // ── Muestra la notificación en la barra de estado ─────────────────────────
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (k, v) -> putExtra(k, v) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.splash_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val TAG        = "FCMService"
        const val CHANNEL_ID = "roomtracker_channel"
    }
}
