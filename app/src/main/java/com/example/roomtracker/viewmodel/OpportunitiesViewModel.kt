package com.example.roomtracker.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.ContentRepository
import com.example.roomtracker.model.OportunidadJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OpportunitiesViewModel(application: Application) : AndroidViewModel(application) {

    sealed class OpportunitiesState {
        object Loading : OpportunitiesState()
        data class Success(val oportunidades: List<OportunidadJson>) : OpportunitiesState()
        data class Error(val message: String) : OpportunitiesState()
    }

    private val repository = ContentRepository()

    private val _state = MutableStateFlow<OpportunitiesState>(OpportunitiesState.Loading)
    val state: StateFlow<OpportunitiesState> = _state.asStateFlow()

    init { loadOpportunities() }

    fun loadOpportunities() {
        viewModelScope.launch {
            _state.value = OpportunitiesState.Loading
            try {
                val carpeta = getApplication<Application>()
                    .getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                    .getString("carpeta_mapas", null)
                    ?: throw Exception("Institución no encontrada")
                _state.value = OpportunitiesState.Success(repository.loadOportunidades(carpeta))
            } catch (e: Exception) {
                android.util.Log.e("OPORTUNIDADES", "Error: ${e.message}")
                _state.value = OpportunitiesState.Error("No se pudieron cargar las oportunidades")
            }
        }
    }
}
