package com.example.roomtracker.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.ContentRepository
import com.example.roomtracker.model.EventoJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventsViewModel(application: Application) : AndroidViewModel(application) {

    sealed class EventsState {
        object Loading : EventsState()
        data class Success(val eventos: List<EventoJson>) : EventsState()
        data class Error(val message: String) : EventsState()
    }

    private val repository = ContentRepository()

    private val _state = MutableStateFlow<EventsState>(EventsState.Loading)
    val state: StateFlow<EventsState> = _state.asStateFlow()

    init { loadEvents() }

    fun loadEvents() {
        viewModelScope.launch {
            _state.value = EventsState.Loading
            try {
                val carpeta = getApplication<Application>()
                    .getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                    .getString("carpeta_mapas", null)
                    ?: throw Exception("Institución no encontrada")
                _state.value = EventsState.Success(repository.loadEventos(carpeta))
            } catch (e: Exception) {
                android.util.Log.e("EVENTS", "Error: ${e.message}")
                _state.value = EventsState.Error("No se pudieron cargar los eventos")
            }
        }
    }
}
