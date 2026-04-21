package com.example.roomtracker.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.model.Institucion
import com.example.roomtracker.supabase
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }

    @Serializable
    private data class UsuarioInsert(
        @SerialName("id_usuario") val idUsuario: String,
        val nombre: String,
        val apellido: String,
        val tipo: String,
        val estado: String
    )

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    private val _verifyState = MutableStateFlow<AuthState>(AuthState.Idle)
    val verifyState: StateFlow<AuthState> = _verifyState.asStateFlow()

    private val _mapFilesReady = MutableStateFlow(false)
    val mapFilesReady: StateFlow<Boolean> = _mapFilesReady.asStateFlow()

    private val _sessionChecked = MutableStateFlow<Boolean?>(null) // null=checking, true=logged in, false=not
    val sessionChecked: StateFlow<Boolean?> = _sessionChecked.asStateFlow()

    private val _forgotState = MutableStateFlow<AuthState>(AuthState.Idle)
    val forgotState: StateFlow<AuthState> = _forgotState.asStateFlow()

    private val _resetState = MutableStateFlow<AuthState>(AuthState.Idle)
    val resetState: StateFlow<AuthState> = _resetState.asStateFlow()

    private val _instituciones = MutableStateFlow<List<Institucion>>(emptyList())
    val instituciones: StateFlow<List<Institucion>> = _instituciones.asStateFlow()

    private var pendingEmail = ""
    private var pendingNombre = ""
    private var pendingApellido = ""
    private var pendingInstitucion: Institucion? = null
    private var recoveryEmail = ""

    init {
        loadInstituciones()
        val context = getApplication<Application>()
        val files = listOf("graph.json", "campus_updated.geojson", "edge_geometry.json")
        android.util.Log.d("RT_INIT", "Archivos locales: ${files.map { it to File(context.filesDir, it).exists() }}")
        val essentialFilesExist = File(context.filesDir, "graph.json").exists() &&
                File(context.filesDir, "campus_updated.geojson").exists()
        _mapFilesReady.value = essentialFilesExist
        viewModelScope.launch {
            try {
                val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                val sessionStr = prefs.getString("session", null)
                android.util.Log.d("RT_SESSION", "Sesión guardada: ${if (sessionStr != null) "SI" else "NO"}")
                _sessionChecked.value = sessionStr != null
                // Si hay sesión pero faltan archivos, re-descargar con la carpeta guardada
                if (sessionStr != null && !_mapFilesReady.value) {
                    val carpeta = prefs.getString("carpeta_mapas", null)
                    android.util.Log.d("RT_SESSION", "Archivos faltantes, carpeta guardada: $carpeta")
                    if (carpeta != null) downloadMapFiles(carpeta)
                }
            } catch (e: Exception) {
                android.util.Log.e("RT_SESSION", "Error: ${e.message}")
                _sessionChecked.value = false
            }
        }
    }

    fun loadInstituciones() {
        viewModelScope.launch {
            try {
                val result = supabase.from("institucion").select().decodeList<Institucion>()
                _instituciones.value = result
            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("Unable to resolve host") == true) 
                    "Error de red: No se pudo conectar con el servidor" 
                else "Error cargando instituciones: ${e.message}"
                android.util.Log.e("AUTH", errorMsg)
            }
        }
    }

    fun register(nombre: String, apellido: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            pendingEmail = email
            pendingNombre = nombre
            pendingApellido = apellido
            var lastException: Exception? = null
            repeat(3) { attempt ->
                try {
                    supabase.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    _registerState.value = AuthState.Success
                    return@launch
                } catch (e: Exception) {
                    lastException = e
                    android.util.Log.e("AUTH_REGISTER", "intento ${attempt + 1}: ${e.message}")
                    val isRetryable = e.message?.contains("timeout", ignoreCase = true) == true ||
                        e.message?.contains("Unable to resolve host") == true ||
                        e.message?.contains("ETIMEDOUT") == true
                    if (isRetryable && attempt < 2) delay(2000L * (attempt + 1))
                    else return@repeat
                }
            }
            val e = lastException!!
            android.util.Log.e("AUTH_REGISTER", "type=${e::class.simpleName} msg=${e.message}", e)
            val errorMsg = when {
                e.message?.contains("Unable to resolve host") == true ||
                e.message?.contains("ETIMEDOUT") == true -> "Sin conexión a internet. Verifica tu Wi-Fi."
                e.message?.contains("timeout", ignoreCase = true) == true -> "El servidor tardó demasiado. Intenta de nuevo."
                e.message?.contains("connect", ignoreCase = true) == true -> "No se pudo conectar: ${e.message}"
                else -> e.message ?: "Error al registrarse"
            }
            _registerState.value = AuthState.Error(errorMsg)
        }
    }

    fun verifyOtp(code: String) {
        viewModelScope.launch {
            _verifyState.value = AuthState.Loading
            try {
                supabase.auth.verifyEmailOtp(
                    type = OtpType.Email.SIGNUP,
                    email = pendingEmail,
                    token = code
                )
                val userId = supabase.auth.currentUserOrNull()?.id
                    ?: throw Exception("No se pudo obtener el usuario")
                supabase.from("usuarios").insert(
                    UsuarioInsert(
                        idUsuario = userId,
                        nombre = pendingNombre,
                        apellido = pendingApellido,
                        tipo = "estudiante",
                        estado = "activo"
                    )
                )
                _verifyState.value = AuthState.Success
            } catch (e: Exception) {
                _verifyState.value = AuthState.Error(e.message ?: "Código incorrecto")
            }
        }
    }

    fun login(email: String, password: String, institucion: Institucion?) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            try {
                if (institucion == null) {
                    _loginState.value = AuthState.Error("Selecciona un campus")
                    return@launch
                }
                val emailDomain = email.substringAfterLast("@")
                if (institucion.dominioCorreo != null && emailDomain != institucion.dominioCorreo) {
                    _loginState.value = AuthState.Error("Tu correo debe ser @${institucion.dominioCorreo}")
                    return@launch
                }
                pendingInstitucion = institucion
                var lastLoginException: Exception? = null
                repeat(3) { attempt ->
                    try {
                        supabase.auth.signInWith(Email) {
                            this.email = email
                            this.password = password
                        }
                        _loginState.value = AuthState.Success
                        val prefs = getApplication<Application>().getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                        prefs.edit().putString("carpeta_mapas", institucion.carpetaMapas).apply()
                        launch { downloadMapFiles(institucion.carpetaMapas) }
                        return@launch
                    } catch (e: Exception) {
                        lastLoginException = e
                        val isRetryable = e.message?.contains("timeout", ignoreCase = true) == true ||
                            e.message?.contains("Unable to resolve host") == true ||
                            e.message?.contains("ETIMEDOUT") == true
                        if (isRetryable && attempt < 2) delay(2000L * (attempt + 1))
                        else return@repeat
                    }
                }
                val e = lastLoginException!!
                val errorMsg = when {
                    e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("ETIMEDOUT") == true -> "Sin conexión a internet. Verifica tu Wi-Fi."
                    e.message?.contains("timeout", ignoreCase = true) == true -> "El servidor tardó demasiado. Intenta de nuevo."
                    else -> e.message ?: "Error al iniciar sesión"
                }
                _loginState.value = AuthState.Error(errorMsg)
            } catch (e: Exception) {
                _loginState.value = AuthState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    private suspend fun downloadMapFiles(carpeta: String?) {
        val context = getApplication<Application>()
        val files = listOf("graph.json", "campus_updated.geojson", "edge_geometry.json")

        // Si ya están todos descargados de una sesión previa, no descargar de nuevo
        if (files.all { File(context.filesDir, it).exists() }) {
            android.util.Log.d("MAP_DOWNLOAD", "Archivos ya existentes, usando caché local")
            _mapFilesReady.value = true
            return
        }

        if (carpeta == null) {
            android.util.Log.w("MAP_DOWNLOAD", "carpeta_mapas es null — sin archivos que descargar")
            return
        }

        files.forEach { fileName ->
            val localFile = File(context.filesDir, fileName)
            if (!localFile.exists()) {
                try {
                    android.util.Log.d("MAP_DOWNLOAD", "Intentando descargar $carpeta/$fileName desde Supabase...")
                    val bytes = supabase.storage["mapas"].downloadPublic("$carpeta/$fileName")
                    localFile.writeBytes(bytes)
                    android.util.Log.d("MAP_DOWNLOAD", "OK: $fileName descargado de Supabase")
                } catch (e: Exception) {
                    android.util.Log.e("MAP_DOWNLOAD", "Error en Supabase para $fileName, usando copia de assets local: ${e.message}")
                    try {
                        context.assets.open(fileName).use { input ->
                            localFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        android.util.Log.d("MAP_DOWNLOAD", "OK: $fileName copiado desde assets")
                    } catch (assetEx: Exception) {
                        android.util.Log.e("MAP_DOWNLOAD", "Error crítico: $fileName no está ni en Supabase ni en assets")
                    }
                }
            }
        }

        val essential = File(context.filesDir, "graph.json").exists() &&
                File(context.filesDir, "campus_updated.geojson").exists()
        android.util.Log.d("MAP_DOWNLOAD", "mapFilesReady = $essential")
        _mapFilesReady.value = essential
    }

    private fun clearMapFiles() {
        val context = getApplication<Application>()
        listOf("graph.json", "campus_updated.geojson", "edge_geometry.json").forEach { fileName ->
            File(context.filesDir, fileName).delete()
        }
        _mapFilesReady.value = false
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotState.value = AuthState.Loading
            try {
                recoveryEmail = email
                supabase.auth.resetPasswordForEmail(email)
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
                supabase.auth.verifyEmailOtp(
                    type = OtpType.Email.RECOVERY,
                    email = recoveryEmail,
                    token = otp
                )
                supabase.auth.updateUser { password = newPassword }
                try { supabase.auth.signOut() } catch (_: Exception) {}
                _resetState.value = AuthState.Success
            } catch (e: Exception) {
                _resetState.value = AuthState.Error(e.message ?: "Código incorrecto o contraseña inválida")
            }
        }
    }

    fun resetForgotState() { _forgotState.value = AuthState.Idle }
    fun resetResetState() { _resetState.value = AuthState.Idle }

    fun logout() {
        viewModelScope.launch {
            try { supabase.auth.signOut() } catch (_: Exception) {}
            clearMapFiles()
            pendingInstitucion = null
            _loginState.value = AuthState.Idle
            _registerState.value = AuthState.Idle
            _verifyState.value = AuthState.Idle
        }
    }

    fun resetLoginState() { _loginState.value = AuthState.Idle }
    fun resetRegisterState() { _registerState.value = AuthState.Idle }
    fun resetVerifyState() { _verifyState.value = AuthState.Idle }
}
