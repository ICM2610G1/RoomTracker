package com.example.roomtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.OperadorRepository
import com.example.roomtracker.model.ConversacionEstudiante
import com.example.roomtracker.model.OperadoresServicio
import com.example.roomtracker.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OperadorViewModel(application: Application) : AndroidViewModel(application) {

    sealed class OperadorState {
        object Loading : OperadorState()
        data class Success(val conversaciones: List<ConversacionEstudiante>) : OperadorState()
        data class Error(val message: String) : OperadorState()
    }

    private val repository = OperadorRepository()

    private val _state = MutableStateFlow<OperadorState>(OperadorState.Loading)
    val state: StateFlow<OperadorState> = _state.asStateFlow()

    val operadorNombre: String
        get() = OperadoresServicio.nombrePorId(supabase.auth.currentUserOrNull()?.id ?: "") ?: "Operador"

    init { loadConversaciones() }

    fun loadConversaciones() {
        viewModelScope.launch {
            _state.value = OperadorState.Loading
            try {
                val myId = supabase.auth.currentUserOrNull()?.id
                    ?: throw Exception("No autenticado")
                val conversaciones = repository.loadConversaciones(myId)
                    .sortedByDescending { it.lastTime }
                    .map { it.copy(lastTime = formatTime(it.lastTime)) }
                _state.value = OperadorState.Success(conversaciones)
            } catch (e: Exception) {
                android.util.Log.e("OPERADOR", "Error: ${e.message}")
                _state.value = OperadorState.Error("No se pudieron cargar las conversaciones")
            }
        }
    }

    private fun formatTime(isoDate: String): String = try {
        val sdf  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(isoDate.substringBefore("+").substringBefore("."))
        SimpleDateFormat("d MMM · h:mm a", Locale("es", "CO")).format(date ?: Date())
    } catch (_: Exception) { isoDate.take(10) }
}
