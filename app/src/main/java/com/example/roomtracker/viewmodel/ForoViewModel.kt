package com.example.roomtracker.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.ForoRepository
import com.example.roomtracker.model.ForoMensajeUi
import com.example.roomtracker.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ForoViewModel(application: Application) : AndroidViewModel(application) {

    sealed class ForoState {
        object Loading : ForoState()
        data class Success(val mensajes: List<ForoMensajeUi>, val yaPublicoHoy: Boolean) : ForoState()
        data class Error(val message: String) : ForoState()
    }

    sealed class PublicarState {
        object Idle    : PublicarState()
        object Loading : PublicarState()
        object Success : PublicarState()
        data class Error(val message: String) : PublicarState()
    }

    private val repository = ForoRepository()

    private val _state          = MutableStateFlow<ForoState>(ForoState.Loading)
    val state: StateFlow<ForoState> = _state.asStateFlow()

    private val _publicarState  = MutableStateFlow<PublicarState>(PublicarState.Idle)
    val publicarState: StateFlow<PublicarState> = _publicarState.asStateFlow()

    private val institucion: String
        get() = getApplication<Application>()
            .getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
            .getString("carpeta_mapas", "javeriana") ?: "javeriana"

    init { loadMensajes() }

    fun loadMensajes() {
        viewModelScope.launch {
            _state.value = ForoState.Loading
            try {
                val uid  = supabase.auth.currentUserOrNull()?.id ?: ""
                val rows = repository.loadMensajes(institucion)
                val hoy  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val yaPublicoHoy = rows.any { it.idUsuario == uid && it.fechaPublicacion.startsWith(hoy) }
                val ui = rows.map { m ->
                    ForoMensajeUi(m.id, m.email, m.nombre, m.contenido, formatFecha(m.fechaPublicacion), m.idUsuario == uid)
                }
                _state.value = ForoState.Success(ui, yaPublicoHoy)
            } catch (e: Exception) {
                _state.value = ForoState.Error("No se pudo cargar el foro: ${e.message}")
            }
        }
    }

    fun publicar(contenido: String) {
        viewModelScope.launch {
            _publicarState.value = PublicarState.Loading
            try {
                val user = supabase.auth.currentUserOrNull()
                    ?: throw Exception("No estás autenticado")
                val (nombre, apellido) = repository.getPerfilNombre(user.id)
                repository.publicar(
                    uid         = user.id,
                    email       = user.email ?: "",
                    nombre      = "$nombre $apellido".trim(),
                    contenido   = contenido,
                    institucion = institucion
                )
                _publicarState.value = PublicarState.Success
                loadMensajes()
            } catch (e: Exception) {
                _publicarState.value = PublicarState.Error(e.message ?: "Error al publicar")
            }
        }
    }

    fun resetPublicarState() { _publicarState.value = PublicarState.Idle }

    private fun formatFecha(iso: String): String = try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date  = input.parse(iso.substringBefore("+").substringBefore("."))!!
        val hoy   = Calendar.getInstance()
        val fecha = Calendar.getInstance().apply { time = date }
        when {
            hoy.get(Calendar.DATE)  == fecha.get(Calendar.DATE) &&
            hoy.get(Calendar.MONTH) == fecha.get(Calendar.MONTH) ->
                "Hoy · " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("d MMM · h:mm a", Locale("es", "CO")).format(date)
        }
    } catch (_: Exception) { iso.take(10) }
}
