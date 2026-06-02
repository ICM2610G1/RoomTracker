package com.example.roomtracker.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.ChatRepository
import com.example.roomtracker.model.ConversacionServicio
import com.example.roomtracker.model.MensajeItem
import com.example.roomtracker.model.OperadoresServicio
import com.example.roomtracker.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application)

    private val _conversaciones = MutableStateFlow<List<ConversacionServicio>>(emptyList())
    val conversaciones: StateFlow<List<ConversacionServicio>> = _conversaciones.asStateFlow()

    private val _mensajes = MutableStateFlow<List<MensajeItem>>(emptyList())
    val mensajes: StateFlow<List<MensajeItem>> = _mensajes.asStateFlow()

    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId: StateFlow<String?> = _currentChatId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val currentUserId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: ""

    init { loadConversaciones() }

    fun loadConversaciones() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = currentUserId
                if (uid.isEmpty()) {
                    _conversaciones.value = OperadoresServicio.lista.map { op ->
                        ConversacionServicio(op, null, "Escribe tu mensaje...", "", false)
                    }
                    return@launch
                }
                val chats = repository.loadChatsDeServicio(uid)
                _conversaciones.value = OperadoresServicio.lista.map { operador ->
                    val chat = chats.firstOrNull { it.contextoId == operador.idUsuario }
                    if (chat != null) {
                        try {
                            val ultimo = repository.loadLastMessage(chat.idChat)
                            ConversacionServicio(
                                operador    = operador,
                                idChat      = chat.idChat,
                                lastMessage = ultimo?.contenidoTexto ?: "Sin mensajes aún",
                                lastTime    = ultimo?.fechaEnvio?.let { formatTime(it) } ?: "",
                                unread      = false
                            )
                        } catch (_: Exception) {
                            ConversacionServicio(operador, chat.idChat, "Sin mensajes aún", "", false)
                        }
                    } else {
                        ConversacionServicio(operador, null, "Escribe tu primer mensaje...", "", false)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CHAT", "Error cargando conversaciones: ${e.message}")
                _conversaciones.value = OperadoresServicio.lista.map { op ->
                    ConversacionServicio(op, null, "Escribe tu mensaje...", "", false)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun openOrCreateChat(operadorId: String, onChatReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val uid = currentUserId
                if (uid.isEmpty()) return@launch
                val existing = repository.findChat(uid, operadorId)
                val chatId = existing?.idChat ?: repository.createChat(uid, operadorId)
                _currentChatId.value = chatId
                loadMessages(chatId)
                onChatReady(chatId)
            } catch (e: Exception) {
                _errorMessage.value = "Error abriendo chat: ${e.message?.lines()?.firstOrNull()}"
            }
        }
    }

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            try {
                val uid = currentUserId
                _mensajes.value = repository.loadMessages(chatId).map { m ->
                    MensajeItem(
                        idMensaje      = m.idMensaje,
                        contenidoTexto = m.contenidoTexto ?: "",
                        tipo           = m.tipo,
                        esMio          = m.idEmisor == uid,
                        fechaEnvio     = m.fechaEnvio?.let { formatTime(it) } ?: ""
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("CHAT", "Error cargando mensajes: ${e.message}")
            }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        viewModelScope.launch {
            try {
                val uid = currentUserId
                if (uid.isEmpty() || text.isBlank()) return@launch
                repository.sendTextMessage(chatId, uid, text)
                loadMessages(chatId)
            } catch (e: Exception) {
                _errorMessage.value = "Error enviando mensaje: ${e.message?.lines()?.firstOrNull()}"
            }
        }
    }

    fun sendImageMessage(chatId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val uid = currentUserId
                if (uid.isEmpty()) return@launch
                repository.sendImageMessage(chatId, uid, imageUri)
                loadMessages(chatId)
            } catch (e: Exception) {
                _errorMessage.value = "Error enviando imagen: ${e.message?.lines()?.firstOrNull()}"
                android.util.Log.e("CHAT", "Image upload error: ${e.message}", e)
            }
        }
    }

    fun setChatDirect(chatId: String) {
        _currentChatId.value = chatId
        loadMessages(chatId)
    }

    fun clearMessages() {
        _mensajes.value     = emptyList()
        _currentChatId.value = null
    }

    fun clearError() { _errorMessage.value = null }

    private fun formatTime(isoDate: String): String = try {
        val sdf  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(isoDate.substringBefore("+").substringBefore("."))
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date ?: Date())
    } catch (_: Exception) { isoDate.take(5) }
}
