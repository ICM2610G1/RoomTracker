package com.example.roomtracker.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.HandlerThread
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.roomtracker.MainActivity
import com.example.roomtracker.R
import com.example.roomtracker.SharedPrefsSessionManager
import com.example.roomtracker.supabase
import com.google.android.gms.location.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Foreground service que mantiene el GPS activo y sube la ubicación a Supabase
 * incluso cuando el app está en segundo plano o el usuario lo cierra desde recientes.
 */
class LocationForegroundService : Service() {

    companion object {
        const val CHANNEL_ID      = "rt_location_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START    = "com.example.roomtracker.ACTION_START_LOCATION"
        const val ACTION_STOP     = "com.example.roomtracker.ACTION_STOP_LOCATION"

        fun start(context: Context) {
            val i = Intent(context, LocationForegroundService::class.java)
                .apply { action = ACTION_START }
            context.startForegroundService(i)
        }

        fun stop(context: Context) {
            cancelRestartAlarm(context)
            val i = Intent(context, LocationForegroundService::class.java)
                .apply { action = ACTION_STOP }
            context.startService(i)
        }

        private fun restartIntent(context: Context) =
            PendingIntent.getService(
                context, 99,
                Intent(context, LocationForegroundService::class.java)
                    .apply { action = ACTION_START },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        private fun cancelRestartAlarm(context: Context) {
            context.getSystemService(AlarmManager::class.java)
                ?.cancel(restartIntent(context))
        }
    }

    // Hilo dedicado para GPS — independiente del main looper de la UI
    private lateinit var handlerThread: HandlerThread
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        handlerThread = HandlerThread("RT_LocationThread").also { it.start() }
        fusedClient   = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        // Restaurar la sesión de Supabase en cuanto empiece el proceso
        serviceScope.launch { restoreSession() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification())
        startLocationUpdates()
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        android.util.Log.d("LOC_SERVICE", "onTaskRemoved — programando reinicio")
        getSystemService(AlarmManager::class.java)?.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 2_000L,
            restartIntent(this)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
        serviceScope.cancel()
        if (::handlerThread.isInitialized && handlerThread.isAlive) handlerThread.quit()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ─── Sesión ───────────────────────────────────────────────────────────────

    /**
     * Carga la sesión guardada en SharedPreferences e importa el token en el SDK
     * para que currentUserOrNull() no devuelva null tras un reinicio del proceso.
     */
    private suspend fun restoreSession() {
        if (supabase.auth.currentUserOrNull() != null) return   // ya está cargada
        try {
            val session = SharedPrefsSessionManager(applicationContext).loadSession()
                ?: return
            supabase.auth.importSession(session, autoRefresh = true)
            android.util.Log.d("LOC_SERVICE", "Sesión restaurada: ${supabase.auth.currentUserOrNull()?.id}")
        } catch (e: Exception) {
            android.util.Log.e("LOC_SERVICE", "Error restaurando sesión: ${e.message}")
        }
    }

    // ─── GPS ──────────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val ok = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!ok) { stopSelf(); return }

        locationCallback?.let { fusedClient.removeLocationUpdates(it) }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10_000L
        ).setMinUpdateIntervalMillis(5_000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val compartiendo = getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
                    .getBoolean("compartiendo", true)
                if (compartiendo) uploadLocation(loc.latitude, loc.longitude)
            }
        }

        fusedClient.requestLocationUpdates(request, locationCallback!!, handlerThread.looper)
        android.util.Log.d("LOC_SERVICE", "GPS activo en HandlerThread")
    }

    // ─── Upload ───────────────────────────────────────────────────────────────

    private fun uploadLocation(lat: Double, lng: Double) {
        serviceScope.launch {
            // Si la sesión aún no cargó, esperamos y reintentamos una vez
            var uid = supabase.auth.currentUserOrNull()?.id
            if (uid == null) {
                restoreSession()
                delay(1_500L)
                uid = supabase.auth.currentUserOrNull()?.id
            }
            if (uid == null) {
                android.util.Log.w("LOC_SERVICE", "Sin sesión activa, omitiendo upload")
                return@launch
            }

            try {
                @Serializable
                data class UbicacionUpsert(
                    @SerialName("id_usuario") val idUsuario: String,
                    val latitud: Double,
                    val longitud: Double
                )
                supabase.from("ubicacion_usuario").upsert(
                    UbicacionUpsert(uid, lat, lng)
                ) { onConflict = "id_usuario" }
                android.util.Log.d("LOC_SERVICE", "Subido: $lat, $lng")
            } catch (e: Exception) {
                android.util.Log.e("LOC_SERVICE", "Error upload: ${e.message}")
            }
        }
    }

    // ─── Notificación ─────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val ch = NotificationChannel(CHANNEL_ID, "Ubicación activa",
            NotificationManager.IMPORTANCE_LOW).apply {
            description = "Mantiene tu posición visible para tus amigos"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("RoomTracker activo")
        .setContentText("Compartiendo ubicación con tus amigos")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        .setContentIntent(
            PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java)
                    .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
                PendingIntent.FLAG_IMMUTABLE)
        )
        .build()
}
