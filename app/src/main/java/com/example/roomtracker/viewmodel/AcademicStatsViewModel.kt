package com.example.roomtracker.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.AcademicRepository
import com.example.roomtracker.model.EstadisticasJson
import com.example.roomtracker.model.MateriaJson
import com.example.roomtracker.model.recalcular
import com.example.roomtracker.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AcademicStatsViewModel(application: Application) : AndroidViewModel(application) {

    sealed class StatsState {
        object Loading : StatsState()
        data class Success(val data: EstadisticasJson) : StatsState()
        data class Error(val message: String) : StatsState()
    }

    sealed class SaveState {
        object Idle    : SaveState()
        object Saving  : SaveState()
        object Saved   : SaveState()
        data class Error(val message: String) : SaveState()
    }

    private val repository = AcademicRepository()

    private val _statsState = MutableStateFlow<StatsState>(StatsState.Loading)
    val statsState: StateFlow<StatsState> = _statsState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private var carpeta: String? = null

    init { loadStats() }

    fun loadStats() {
        viewModelScope.launch {
            _statsState.value = StatsState.Loading
            try {
                val uid     = supabase.auth.currentUserOrNull()?.id
                    ?: throw Exception("No hay sesión activa")
                val carpetaActual = getApplication<Application>()
                    .getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                    .getString("carpeta_mapas", null)
                    ?: throw Exception("Institución no encontrada")
                carpeta = carpetaActual
                _statsState.value = StatsState.Success(repository.loadEstadisticas(uid, carpetaActual))
            } catch (e: Exception) {
                android.util.Log.e("STATS", "Error: ${e.message}")
                _statsState.value = StatsState.Error(
                    if (e.message?.contains("404") == true || e.message?.contains("not found", ignoreCase = true) == true)
                        "No tienes estadísticas disponibles aún"
                    else "Error al cargar estadísticas: ${e.message}"
                )
            }
        }
    }

    /**
     * Reemplaza las materias del periodo (año/ciclo) indicado, recalcula todos
     * los promedios derivados y persiste el JSON actualizado en Storage.
     */
    fun guardarMaterias(anio: Int, ciclo: Int, materias: List<MateriaJson>) {
        val actual  = (_statsState.value as? StatsState.Success)?.data ?: return
        val carpetaActual = carpeta ?: run {
            _saveState.value = SaveState.Error("No se encontró la institución")
            return
        }
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                val uid = supabase.auth.currentUserOrNull()?.id
                    ?: throw Exception("No hay sesión activa")
                val nuevosPeriodos = actual.periodos.map { p ->
                    if (p.anio == anio && p.ciclo == ciclo) p.copy(materias = materias) else p
                }
                val actualizado = actual.copy(periodos = nuevosPeriodos).recalcular()
                repository.saveEstadisticas(uid, carpetaActual, actualizado)
                _statsState.value = StatsState.Success(actualizado)
                _saveState.value  = SaveState.Saved
            } catch (e: Exception) {
                android.util.Log.e("STATS", "Error guardando: ${e.message}")
                _saveState.value = SaveState.Error(
                    if (e.message?.contains("403") == true || e.message?.contains("policy", ignoreCase = true) == true)
                        "No tienes permiso para guardar. Revisa las políticas del bucket 'notas'."
                    else "Error al guardar: ${e.message}"
                )
            }
        }
    }

    fun resetSaveState() { _saveState.value = SaveState.Idle }
}
