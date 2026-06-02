package com.example.roomtracker.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.LocationRepository
import com.example.roomtracker.model.AmigoEnMapa
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.example.roomtracker.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository  = LocationRepository()
    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)
    private val prefs       = application.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)

    private val _miUbicacion    = MutableStateFlow<LatLng?>(null)
    val miUbicacion: StateFlow<LatLng?> = _miUbicacion.asStateFlow()

    private val _amigosEnMapa   = MutableStateFlow<List<AmigoEnMapa>>(emptyList())
    val amigosEnMapa: StateFlow<List<AmigoEnMapa>> = _amigosEnMapa.asStateFlow()

    private val _compartiendo   = MutableStateFlow(prefs.getBoolean("compartiendo", true))
    val compartiendo: StateFlow<Boolean> = _compartiendo.asStateFlow()

    private val _sesionInvalidada = MutableStateFlow(false)
    val sesionInvalidada: StateFlow<Boolean> = _sesionInvalidada.asStateFlow()

    private var locationCallback: LocationCallback? = null

    val currentUserId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: ""

    init {
        startLocationUpdates()
        viewModelScope.launch {
            while (true) {
                cargarAmigosEnMapa()
                verificarSesion()
                delay(5_000L)
            }
        }
    }

    private fun verificarSesion() {
        val uid = currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            try {
                val ctx     = getApplication<Application>()
                val myToken = ctx.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                    .getString("my_session_token", null) ?: return@launch
                val serverToken = repository.getSessionToken(uid)
                if (serverToken != null && serverToken != myToken) {
                    android.util.Log.w("SESSION", "Sesión invalidada — otro dispositivo inició sesión")
                    _sesionInvalidada.value = true
                    supabase.auth.signOut()
                    try { com.example.roomtracker.service.LocationForegroundService.stop(getApplication()) } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                android.util.Log.e("SESSION", "Error verificando sesión: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val ctx = getApplication<Application>()
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                _miUbicacion.value = LatLng(loc.latitude, loc.longitude)
                if (_compartiendo.value) uploadMiUbicacion(loc.latitude, loc.longitude)
            }
        }
        fusedClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    private fun uploadMiUbicacion(lat: Double, lng: Double) {
        val uid = currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            try { repository.upsertLocation(uid, lat, lng) }
            catch (e: Exception) { android.util.Log.e("UPLOAD_LOC", "ERROR: ${e.message}", e) }
        }
    }

    fun setCompartiendo(compartir: Boolean) {
        _compartiendo.value = compartir
        prefs.edit().putBoolean("compartiendo", compartir).apply()
        if (!compartir) {
            viewModelScope.launch {
                val uid = currentUserId
                if (uid.isEmpty()) return@launch
                try { repository.deleteLocation(uid) }
                catch (e: Exception) { android.util.Log.e("LOCATION", "Error borrando ubicación: ${e.message}") }
            }
        }
    }

    private fun cargarAmigosEnMapa() {
        val uid = currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            try {
                val amistades     = repository.loadFriendships()
                    .filter { it.idUsuario1 == uid || it.idUsuario2 == uid }
                val amigosVisibles = mutableListOf<AmigoEnMapa>()
                for (amistad in amistades) {
                    val otroId     = if (amistad.idUsuario1 == uid) amistad.idUsuario2 else amistad.idUsuario1
                    val yoPuedoVer = if (amistad.idUsuario1 == uid) amistad.puedeVer2 else amistad.puedeVer1
                    if (!yoPuedoVer) continue
                    try {
                        val ubicacion = repository.loadUserLocation(otroId) ?: continue
                        val usuario   = repository.loadUser(otroId) ?: continue
                        amigosVisibles.add(
                            AmigoEnMapa(otroId, usuario.nombre, usuario.apellido, LatLng(ubicacion.latitud, ubicacion.longitud))
                        )
                    } catch (_: Exception) {}
                }
                _amigosEnMapa.value = amigosVisibles
            } catch (e: Exception) {
                android.util.Log.e("MAPA_AMIGOS", "EXCEPCION: ${e.message}", e)
            }
        }
    }

    fun togglePrivacidad(amistadId: String, esUsuario1: Boolean, nuevoValor: Boolean) {
        viewModelScope.launch {
            try {
                val campo = if (esUsuario1) "puede_ver_1" else "puede_ver_2"
                repository.togglePrivacidad(amistadId, campo, nuevoValor)
            } catch (e: Exception) {
                android.util.Log.e("LOCATION", "Error toggleando privacidad: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
    }
}
