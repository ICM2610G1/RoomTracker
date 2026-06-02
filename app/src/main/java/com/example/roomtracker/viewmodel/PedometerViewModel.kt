package com.example.roomtracker.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.PedometerRepository
import com.example.roomtracker.model.DiaStats
import com.example.roomtracker.model.DistanciaJson
import com.example.roomtracker.supabase
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PedometerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG             = "PEDOMETER"
        private const val METROS_POR_PASO = 0.75
        private val CAMPUS_DEFAULT = listOf(
            Pair(4.6298, -74.0672), Pair(4.6298, -74.0598),
            Pair(4.6228, -74.0598), Pair(4.6228, -74.0672)
        )
    }

    private val repository = PedometerRepository()

    // ── States ────────────────────────────────────────────────────────────────
    private val _stepsToday        = MutableStateFlow(0)
    val stepsToday: StateFlow<Int> = _stepsToday.asStateFlow()

    private val _distanciaTotal        = MutableStateFlow(0.0)
    val distanciaTotal: StateFlow<Double> = _distanciaTotal.asStateFlow()

    private val _historial             = MutableStateFlow<List<DiaStats>>(emptyList())
    val historial: StateFlow<List<DiaStats>> = _historial.asStateFlow()

    private val _dentroCampus          = MutableStateFlow(false)
    val dentroCampus: StateFlow<Boolean> = _dentroCampus.asStateFlow()

    private val _isLoading             = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean>  = _isLoading.asStateFlow()

    // ── Internos ──────────────────────────────────────────────────────────────
    private val today              = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private var campusPolygon      = CAMPUS_DEFAULT
    private var stepsSinceLastSave = 0

    private val sensorManager  = application.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor     = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val fusedClient    = LocationServices.getFusedLocationProviderClient(application)

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR && _dentroCampus.value) {
                _stepsToday.value += 1
                stepsSinceLastSave++
                if (stepsSinceLastSave >= 50) { stepsSinceLastSave = 0; saveStats() }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            _dentroCampus.value = isInsidePolygon(loc.latitude, loc.longitude, campusPolygon)
        }
    }

    init {
        campusPolygon = loadCampusPolygonFromGeoJson() ?: CAMPUS_DEFAULT
        loadStats()
        startStepSensor()
        startLocationUpdates()
    }

    private fun loadCampusPolygonFromGeoJson(): List<Pair<Double, Double>>? {
        return try {
            val file = File(getApplication<Application>().filesDir, "campus_updated.geojson")
            if (!file.exists()) return null
            val root     = JSONObject(file.readText())
            val features = root.getJSONArray("features")
            var bestPolygon: List<Pair<Double, Double>>? = null
            var bestArea = 0.0
            for (i in 0 until features.length()) {
                val feature  = features.getJSONObject(i)
                if (feature.getJSONObject("geometry").getString("type") == "Polygon") {
                    val coords = feature.getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(0)
                    val points = (0 until coords.length()).map { j ->
                        val pt = coords.getJSONArray(j)
                        Pair(pt.getDouble(1), pt.getDouble(0))
                    }
                    val area = polygonArea(points)
                    if (area > bestArea) { bestArea = area; bestPolygon = points }
                }
            }
            bestPolygon
        } catch (e: Exception) { Log.e(TAG, "Error cargando GeoJSON: ${e.message}"); null }
    }

    private fun isInsidePolygon(lat: Double, lng: Double, polygon: List<Pair<Double, Double>>): Boolean {
        if (polygon.isEmpty()) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val yi = polygon[i].first; val xi = polygon[i].second
            val yj = polygon[j].first; val xj = polygon[j].second
            if ((yi > lat) != (yj > lat) && lng < (xj - xi) * (lat - yi) / (yj - yi) + xi)
                inside = !inside
            j = i
        }
        return inside
    }

    private fun polygonArea(polygon: List<Pair<Double, Double>>): Double {
        var area = 0.0
        val n    = polygon.size
        for (i in polygon.indices) {
            val j = (i + 1) % n
            area += polygon[i].second * polygon[j].first
            area -= polygon[j].second * polygon[i].first
        }
        return kotlin.math.abs(area) / 2.0
    }

    private fun startStepSensor() {
        stepSensor?.let { sensorManager.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_FASTEST) }
            ?: Log.w(TAG, "Dispositivo no tiene sensor de pasos")
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L).build()
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid   = supabase.auth.currentUserOrNull()?.id ?: return@launch
                val stats = repository.loadStats(uid) ?: return@launch
                val todayStats = stats.historial.find { it.fecha == today }
                _stepsToday.value     = todayStats?.pasos ?: 0
                _distanciaTotal.value = stats.totalDistanciaMetros
                _historial.value      = stats.historial.takeLast(7).reversed()
            } catch (e: Exception) {
                Log.d(TAG, "Sin datos previos: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveStats() {
        viewModelScope.launch {
            try {
                val uid      = supabase.auth.currentUserOrNull()?.id ?: return@launch
                val pasosHoy = _stepsToday.value
                val distHoy  = pasosHoy * METROS_POR_PASO
                val histActual = _historial.value.toMutableList()
                val idx        = histActual.indexOfFirst { it.fecha == today }
                val diaStats   = DiaStats(today, pasosHoy, distHoy)
                if (idx >= 0) histActual[idx] = diaStats else histActual.add(0, diaStats)
                val histFinal = histActual.take(30)
                _historial.value      = histFinal.take(7)
                _distanciaTotal.value = histFinal.sumOf { it.distanciaMetros }
                val statsObj = DistanciaJson(
                    idUsuario            = uid,
                    totalPasos           = histFinal.sumOf { it.pasos },
                    totalDistanciaMetros = _distanciaTotal.value,
                    historial            = histFinal
                )
                repository.saveStats(uid, statsObj)
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando stats: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(stepListener)
        fusedClient.removeLocationUpdates(locationCallback)
        saveStats()
    }
}
