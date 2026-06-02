package com.example.roomtracker.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.AuthRepository
import com.example.roomtracker.model.Institucion
import com.example.roomtracker.model.UserProfile
import com.example.roomtracker.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    sealed class AuthState {
        object Idle    : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val repository = AuthRepository(application)

    // ── States ────────────────────────────────────────────────────────────────
    private val _loginState    = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    private val _verifyState   = MutableStateFlow<AuthState>(AuthState.Idle)
    val verifyState: StateFlow<AuthState> = _verifyState.asStateFlow()

    private val _forgotState   = MutableStateFlow<AuthState>(AuthState.Idle)
    val forgotState: StateFlow<AuthState> = _forgotState.asStateFlow()

    private val _resetState    = MutableStateFlow<AuthState>(AuthState.Idle)
    val resetState: StateFlow<AuthState> = _resetState.asStateFlow()

    private val _mapFilesReady = MutableStateFlow(false)
    val mapFilesReady: StateFlow<Boolean> = _mapFilesReady.asStateFlow()

    private val _sessionChecked = MutableStateFlow<Boolean?>(null)
    val sessionChecked: StateFlow<Boolean?> = _sessionChecked.asStateFlow()

    private val _userTipo      = MutableStateFlow<String?>(null)
    val userTipo: StateFlow<String?> = _userTipo.asStateFlow()

    private val _instituciones = MutableStateFlow<List<Institucion>>(emptyList())
    val instituciones: StateFlow<List<Institucion>> = _instituciones.asStateFlow()

    private val _userProfile   = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _profileUpdateState = MutableStateFlow<AuthState>(AuthState.Idle)
    val profileUpdateState: StateFlow<AuthState> = _profileUpdateState.asStateFlow()

    // ── Temporales ────────────────────────────────────────────────────────────
    private var pendingEmail      = ""
    private var pendingNombre     = ""
    private var pendingApellido   = ""
    private var pendingInstitucion: Institucion? = null
    private var recoveryEmail     = ""

    // ── Init ──────────────────────────────────────────────────────────────────
    init {
        loadInstituciones()
        val ctx = getApplication<Application>()
        _mapFilesReady.value = repository.mapFilesExist()
        viewModelScope.launch {
            try {
                val prefs = ctx.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                val sessionStr = prefs.getString("session", null)
                _userTipo.value = prefs.getString("user_tipo", "estudiante") ?: "estudiante"
                _sessionChecked.value = sessionStr != null
                if (sessionStr != null) {
                    val carpeta = prefs.getString("carpeta_mapas", null)
                    val allFiles = listOf("graph.json", "campus_updated.geojson", "edge_geometry.json", "pois.json", "edificios.json")
                    if (allFiles.any { !File(ctx.filesDir, it).exists() } && carpeta != null) {
                        repository.downloadMapFiles(carpeta)
                        _mapFilesReady.value = repository.mapFilesExist()
                    }
                }
            } catch (e: Exception) {
                _sessionChecked.value = false
            }
        }
    }

    // ── Instituciones ─────────────────────────────────────────────────────────
    fun loadInstituciones() {
        viewModelScope.launch {
            try {
                _instituciones.value = repository.loadInstituciones()
            } catch (e: Exception) {
                android.util.Log.e("AUTH", "Error cargando instituciones: ${e.message}")
            }
        }
    }

    // ── Registro ──────────────────────────────────────────────────────────────
    fun register(nombre: String, apellido: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            pendingEmail    = email
            pendingNombre   = nombre
            pendingApellido = apellido
            var lastEx: Exception? = null
            repeat(3) { attempt ->
                try {
                    repository.signUp(email, password)
                    _registerState.value = AuthState.Success
                    return@launch
                } catch (e: Exception) {
                    lastEx = e
                    if (isRetryable(e) && attempt < 2) delay(2000L * (attempt + 1))
                    else return@repeat
                }
            }
            _registerState.value = AuthState.Error(mapError(lastEx!!))
        }
    }

    fun verifyOtp(code: String) {
        viewModelScope.launch {
            _verifyState.value = AuthState.Loading
            try {
                repository.verifyOtp(pendingEmail, code)
                val userId = supabase.auth.currentUserOrNull()?.id
                    ?: throw Exception("No se pudo obtener el usuario")
                val domain  = pendingEmail.substringAfterLast("@")
                val instId  = _instituciones.value.firstOrNull { it.dominioCorreo == domain }?.id
                repository.insertUsuario(userId, pendingNombre, pendingApellido, instId)
                _verifyState.value = AuthState.Success
            } catch (e: Exception) {
                _verifyState.value = AuthState.Error(e.message ?: "Código incorrecto")
            }
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    fun login(email: String, password: String, institucion: Institucion?) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            try {
                if (institucion == null) { _loginState.value = AuthState.Error("Selecciona un campus"); return@launch }
                val emailDomain = email.substringAfterLast("@")
                if (institucion.dominioCorreo != null && emailDomain != institucion.dominioCorreo) {
                    _loginState.value = AuthState.Error("Tu correo debe ser @${institucion.dominioCorreo}")
                    return@launch
                }
                pendingInstitucion = institucion
                var lastEx: Exception? = null
                repeat(3) { attempt ->
                    try {
                        repository.signIn(email, password)
                        val uid = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Login fallido")

                        // Sesión única
                        val newToken = java.util.UUID.randomUUID().toString()
                        getApplication<Application>()
                            .getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                            .edit().putString("my_session_token", newToken).apply()
                        try { repository.saveSessionToken(uid, newToken) } catch (_: Exception) {}

                        val tipo = repository.getUserTipo(uid)
                        repository.saveCarpetaMapas(institucion.carpetaMapas, tipo)
                        _userTipo.value = tipo
                        _loginState.value = AuthState.Success
                        saveFcmToken(uid)
                        if (tipo != "operador") launch { repository.downloadMapFiles(institucion.carpetaMapas); _mapFilesReady.value = repository.mapFilesExist() }
                        return@launch
                    } catch (e: Exception) {
                        lastEx = e
                        if (isRetryable(e) && attempt < 2) delay(2000L * (attempt + 1))
                        else return@repeat
                    }
                }
                _loginState.value = AuthState.Error(mapError(lastEx!!))
            } catch (e: Exception) {
                _loginState.value = AuthState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    // ── Perfil ────────────────────────────────────────────────────────────────
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val user  = supabase.auth.currentUserOrNull() ?: return@launch
                _userProfile.value = repository.loadProfile(user.id, user.email ?: "")
            } catch (e: Exception) {
                android.util.Log.e("AUTH", "Error cargando perfil: ${e.message}")
            }
        }
    }

    fun updateProfile(nombre: String, apellido: String) {
        viewModelScope.launch {
            _profileUpdateState.value = AuthState.Loading
            try {
                val uid = supabase.auth.currentUserOrNull()?.id ?: return@launch
                repository.updateProfile(uid, nombre, apellido)
                _userProfile.value = _userProfile.value?.copy(nombre = nombre, apellido = apellido)
                _profileUpdateState.value = AuthState.Success
            } catch (e: Exception) {
                _profileUpdateState.value = AuthState.Error(e.message ?: "Error actualizando perfil")
            }
        }
    }

    fun uploadProfilePhoto(imageUri: Uri) {
        viewModelScope.launch {
            _profileUpdateState.value = AuthState.Loading
            try {
                val uid = supabase.auth.currentUserOrNull()?.id ?: return@launch
                val url = repository.uploadPhoto(uid, imageUri)
                _userProfile.value = _userProfile.value?.copy(fotoUrl = url)
                _profileUpdateState.value = AuthState.Success
            } catch (e: Exception) {
                _profileUpdateState.value = AuthState.Error("Error subiendo foto: ${e.message?.lines()?.firstOrNull()}")
            }
        }
    }

    fun resetProfileUpdateState() { _profileUpdateState.value = AuthState.Idle }

    // ── Recuperar contraseña ──────────────────────────────────────────────────
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotState.value = AuthState.Loading
            try {
                recoveryEmail = email
                repository.forgotPassword(email)
                _forgotState.value = AuthState.Success
            } catch (e: Exception) {
                _forgotState.value = AuthState.Error(e.message ?: "Error al enviar el correo")
            }
        }
    }

    fun resetPassword(otp: String, newPassword: String) {
        viewModelScope.launch {
            _resetState.value = AuthState.Loading
            try {
                repository.resetPassword(recoveryEmail, otp, newPassword)
                _resetState.value = AuthState.Success
            } catch (e: Exception) {
                _resetState.value = AuthState.Error(e.message ?: "Código incorrecto o contraseña inválida")
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    fun logout() {
        viewModelScope.launch {
            try { repository.signOut() } catch (_: Exception) {}
            try { com.example.roomtracker.service.LocationForegroundService.stop(getApplication()) } catch (_: Exception) {}
            repository.clearMapFiles()
            repository.clearUserTipo()
            pendingInstitucion = null
            _userTipo.value = null
            _loginState.value    = AuthState.Idle
            _registerState.value = AuthState.Idle
            _verifyState.value   = AuthState.Idle
        }
    }

    // ── FCM Token ─────────────────────────────────────────────────────────────
    fun saveFcmToken(uid: String) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                viewModelScope.launch {
                    try {
                        repository.saveFcmToken(uid, token)
                        android.util.Log.d("FCM", "Token guardado tras login uid=$uid")
                    } catch (e: Exception) {
                        android.util.Log.e("FCM", "Error guardando FCM token: ${e.message}")
                    }
                }
            }
    }

    // ── Resets de estado ──────────────────────────────────────────────────────
    fun resetLoginState()    { _loginState.value    = AuthState.Idle }
    fun resetRegisterState() { _registerState.value = AuthState.Idle }
    fun resetVerifyState()   { _verifyState.value   = AuthState.Idle }
    fun resetForgotState()   { _forgotState.value   = AuthState.Idle }
    fun resetResetState()    { _resetState.value    = AuthState.Idle }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun isRetryable(e: Exception) =
        e.message?.contains("timeout", ignoreCase = true) == true ||
        e.message?.contains("Unable to resolve host") == true ||
        e.message?.contains("ETIMEDOUT") == true

    private fun mapError(e: Exception): String = when {
        e.message?.contains("Unable to resolve host") == true ||
        e.message?.contains("ETIMEDOUT") == true -> "Sin conexión a internet. Verifica tu Wi-Fi."
        e.message?.contains("timeout", ignoreCase = true) == true -> "El servidor tardó demasiado. Intenta de nuevo."
        else -> e.message ?: "Error desconocido"
    }
}
