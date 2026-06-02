package com.example.roomtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.FriendsRepository
import com.example.roomtracker.model.AmigoConAmistad
import com.example.roomtracker.model.UsuarioRow
import com.example.roomtracker.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FriendsRepository()

    private val _amigos          = MutableStateFlow<List<AmigoConAmistad>>(emptyList())
    val amigos: StateFlow<List<AmigoConAmistad>> = _amigos.asStateFlow()

    private val _searchResults   = MutableStateFlow<List<UsuarioRow>>(emptyList())
    val searchResults: StateFlow<List<UsuarioRow>> = _searchResults.asStateFlow()

    private val _pendingRequests = MutableStateFlow<List<AmigoConAmistad>>(emptyList())
    val pendingRequests: StateFlow<List<AmigoConAmistad>> = _pendingRequests.asStateFlow()

    private val _sentRequestUserIds = MutableStateFlow<Set<String>>(emptySet())
    val sentRequestUserIds: StateFlow<Set<String>> = _sentRequestUserIds.asStateFlow()

    private val _isLoading       = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage    = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastSearchQuery  = ""

    val currentUserId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: ""

    init {
        loadFriends()
        loadPendingRequests()
        loadSentRequests()
    }

    fun loadSentRequests() {
        viewModelScope.launch {
            try {
                val uid = currentUserId
                if (uid.isEmpty()) return@launch
                val enviadas = repository.loadPendingSent(uid)
                _sentRequestUserIds.value = enviadas.map { it.idUsuario2 }.toSet()
            } catch (e: Exception) {
                android.util.Log.e("SENT_REQ", "Error: ${e.message}")
            }
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid       = currentUserId
                if (uid.isEmpty()) return@launch
                val amistades = repository.loadFriendships("aceptada")
                    .filter { it.idUsuario1 == uid || it.idUsuario2 == uid }

                val result = amistades.mapNotNull { amistad ->
                    val soyUsuario1      = amistad.idUsuario1 == uid
                    val otroId           = if (soyUsuario1) amistad.idUsuario2 else amistad.idUsuario1
                    val meEstoyMostrando = if (soyUsuario1) amistad.puedeVer1 ?: true else amistad.puedeVer2 ?: true
                    try {
                        AmigoConAmistad(
                            amistadId        = amistad.idAmistad,
                            usuario          = repository.loadUser(otroId),
                            esUsuario1       = soyUsuario1,
                            meEstoyMostrando = meEstoyMostrando
                        )
                    } catch (_: Exception) { null }
                }
                _amigos.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Error cargando amigos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            try {
                val uid = currentUserId
                if (uid.isEmpty()) return@launch
                val pendientes = repository.loadPendingReceived(uid)
                _pendingRequests.value = pendientes.mapNotNull { amistad ->
                    try { AmigoConAmistad(amistad.idAmistad, repository.loadUser(amistad.idUsuario1)) }
                    catch (_: Exception) { null }
                }
            } catch (e: Exception) {
                android.util.Log.e("PENDING", "EXCEPCION: ${e.message}", e)
            }
        }
    }

    fun searchUsers(query: String) {
        lastSearchQuery = query
        if (query.isBlank()) { _searchResults.value = emptyList(); return }
        viewModelScope.launch {
            delay(350)
            if (lastSearchQuery != query) return@launch
            try {
                val uid          = currentUserId
                val amigoIds     = _amigos.value.map { it.usuario.idUsuario }.toSet()
                val pendienteIds = _pendingRequests.value.map { it.usuario.idUsuario }.toSet()
                val sentIds      = _sentRequestUserIds.value
                val miInstitucion = repository.getUserInstitucion(uid)

                val todos = repository.loadAllUsers()
                    .filter { it.tipo == "estudiante" && it.estado == "activo" && it.idUsuario != uid }
                    .filter { u -> if (miInstitucion != null) u.idInstitucion == miInstitucion else u.idInstitucion == null }
                    .filter { it.idUsuario !in amigoIds && it.idUsuario !in pendienteIds && it.idUsuario !in sentIds }
                    .filter { u -> u.nombre.contains(query, ignoreCase = true) || u.apellido.contains(query, ignoreCase = true) }

                if (lastSearchQuery == query) _searchResults.value = todos
            } catch (e: Exception) {
                _errorMessage.value = "Error búsqueda: ${e.message}"
            }
        }
    }

    fun sendFriendRequest(targetUserId: String) {
        viewModelScope.launch {
            try {
                val uid = currentUserId
                if (uid.isEmpty()) return@launch
                repository.sendFriendRequest(uid, targetUserId)
                _sentRequestUserIds.value = _sentRequestUserIds.value + targetUserId
            } catch (e: Exception) {
                _errorMessage.value = "Error enviando solicitud: ${e.message}"
            }
        }
    }

    fun removeFriend(amistadId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAmistad(amistadId)
                _amigos.value = _amigos.value.filter { it.amistadId != amistadId }
            } catch (e: Exception) {
                _errorMessage.value = "Error eliminando amigo: ${e.message}"
            }
        }
    }

    fun acceptRequest(amistadId: String) {
        viewModelScope.launch {
            try {
                repository.acceptRequest(amistadId)
                loadFriends()
                loadPendingRequests()
            } catch (e: Exception) {
                _errorMessage.value = "Error aceptando solicitud: ${e.message}"
            }
        }
    }

    fun rejectRequest(amistadId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAmistad(amistadId)
                _pendingRequests.value = _pendingRequests.value.filter { it.amistadId != amistadId }
            } catch (e: Exception) {
                _errorMessage.value = "Error rechazando solicitud: ${e.message}"
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
}
