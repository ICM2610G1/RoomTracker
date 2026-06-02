package com.example.roomtracker.data.repository

import android.app.Application
import android.content.Context
import android.net.Uri
import com.example.roomtracker.model.Institucion
import com.example.roomtracker.model.UserProfile
import com.example.roomtracker.model.UsuarioRow
import com.example.roomtracker.supabase
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

class AuthRepository(private val application: Application) {

    // ── Instituciones ─────────────────────────────────────────────────────────

    suspend fun loadInstituciones(): List<Institucion> =
        supabase.from("institucion").select().decodeList()

    // ── Sesión ────────────────────────────────────────────────────────────────

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun verifyOtp(email: String, token: String) {
        supabase.auth.verifyEmailOtp(
            type  = OtpType.Email.SIGNUP,
            email = email,
            token = token
        )
    }

    suspend fun insertUsuario(
        userId: String,
        nombre: String,
        apellido: String,
        institucionId: String?
    ) {
        @Serializable
        data class UsuarioInsert(
            @SerialName("id_usuario")     val idUsuario: String,
            val nombre: String,
            val apellido: String,
            val tipo: String,
            val estado: String,
            @SerialName("id_institucion") val idInstitucion: String? = null
        )
        supabase.from("usuarios").insert(
            UsuarioInsert(userId, nombre, apellido, "estudiante", "activo", institucionId)
        )
    }

    suspend fun signOut() = supabase.auth.signOut()

    suspend fun forgotPassword(email: String) =
        supabase.auth.resetPasswordForEmail(email)

    suspend fun resetPassword(email: String, otp: String, newPassword: String) {
        supabase.auth.verifyEmailOtp(
            type  = OtpType.Email.RECOVERY,
            email = email,
            token = otp
        )
        supabase.auth.updateUser { password = newPassword }
        try { supabase.auth.signOut() } catch (_: Exception) {}
    }

    // ── Perfil ────────────────────────────────────────────────────────────────

    suspend fun getUserTipo(uid: String): String = try {
        supabase.from("usuarios").select {
            filter { eq("id_usuario", uid) }
        }.decodeSingle<UsuarioRow>().tipo
    } catch (_: Exception) { "estudiante" }

    suspend fun loadProfile(uid: String, email: String): UserProfile {
        val row = supabase.from("usuarios").select {
            filter { eq("id_usuario", uid) }
        }.decodeSingle<UsuarioRow>()
        return UserProfile(row.nombre, row.apellido, email, row.fotoUrl)
    }

    suspend fun updateProfile(uid: String, nombre: String, apellido: String) {
        @Serializable
        data class ProfileUpdate(val nombre: String, val apellido: String)
        supabase.from("usuarios").update(ProfileUpdate(nombre, apellido)) {
            filter { eq("id_usuario", uid) }
        }
    }

    suspend fun uploadPhoto(uid: String, imageUri: Uri): String {
        val bytes = application.contentResolver
            .openInputStream(imageUri)?.use { it.readBytes() }
            ?: throw Exception("No se pudo leer la imagen")
        val path = "javeriana/$uid.jpg"
        try { supabase.storage["avatars"].delete(listOf(path)) } catch (_: Exception) {}
        supabase.storage["avatars"].upload(path, bytes)
        val url = supabase.storage["avatars"].publicUrl(path)
        @Serializable
        data class FotoUpdate(@SerialName("foto_url") val fotoUrl: String)
        supabase.from("usuarios").update(FotoUpdate(url)) {
            filter { eq("id_usuario", uid) }
        }
        return url
    }

    // ── Tokens ────────────────────────────────────────────────────────────────

    suspend fun saveSessionToken(uid: String, token: String) {
        @Serializable
        data class TokenUpdate(@SerialName("session_token") val sessionToken: String)
        supabase.from("usuarios").update(TokenUpdate(token)) {
            filter { eq("id_usuario", uid) }
        }
    }

    suspend fun getSessionToken(uid: String): String? = try {
        supabase.from("usuarios").select {
            filter { eq("id_usuario", uid) }
        }.decodeSingle<UsuarioRow>().sessionToken
    } catch (_: Exception) { null }

    suspend fun saveFcmToken(uid: String, token: String) {
        @Serializable
        data class FcmUpdate(@SerialName("fcm_token") val fcmToken: String)
        supabase.from("usuarios").update(FcmUpdate(token)) {
            filter { eq("id_usuario", uid) }
        }
    }

    // ── Archivos de mapa ──────────────────────────────────────────────────────

    // Archivos de geometría estática: se cachean indefinidamente.
    private val staticMapFiles  = listOf("graph.json", "campus_updated.geojson", "edge_geometry.json")
    // Archivos de contenido que pueden cambiar (URLs de imágenes, descripciones, etc.):
    // se re-descargan siempre que haya conexión para evitar quedar con datos viejos.
    private val dynamicMapFiles = listOf("pois.json", "edificios.json")

    suspend fun downloadMapFiles(carpeta: String?) {
        if (carpeta == null) return

        // Estáticos: solo si aún no existen localmente.
        staticMapFiles.forEach { fileName ->
            val localFile = File(application.filesDir, fileName)
            if (!localFile.exists()) downloadFromStorageOrAsset(carpeta, fileName, localFile)
        }

        // Dinámicos: intentar refrescar siempre desde Storage. Si falla la
        // descarga y ya hay una copia local, se conserva la existente; si no
        // hay copia local, se cae al asset empaquetado.
        dynamicMapFiles.forEach { fileName ->
            val localFile = File(application.filesDir, fileName)
            try {
                val bytes = supabase.storage["mapas"].downloadPublic("$carpeta/$fileName")
                localFile.writeBytes(bytes)
            } catch (_: Exception) {
                if (!localFile.exists()) copyFromAsset(fileName, localFile)
            }
        }
    }

    private suspend fun downloadFromStorageOrAsset(carpeta: String, fileName: String, localFile: File) {
        try {
            val bytes = supabase.storage["mapas"].downloadPublic("$carpeta/$fileName")
            localFile.writeBytes(bytes)
        } catch (_: Exception) {
            copyFromAsset(fileName, localFile)
        }
    }

    private fun copyFromAsset(fileName: String, localFile: File) {
        try {
            application.assets.open(fileName).use { input ->
                localFile.outputStream().use { input.copyTo(it) }
            }
        } catch (_: Exception) {}
    }

    fun clearMapFiles() {
        listOf("graph.json", "campus_updated.geojson", "edge_geometry.json", "pois.json", "edificios.json")
            .forEach { File(application.filesDir, it).delete() }
    }

    fun getCarpetaMapas(): String? =
        application.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
            .getString("carpeta_mapas", null)

    fun saveCarpetaMapas(carpeta: String?, tipo: String) {
        application.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
            .edit()
            .putString("carpeta_mapas", carpeta)
            .putString("user_tipo", tipo)
            .apply()
    }

    fun clearUserTipo() {
        application.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
            .edit().remove("user_tipo").apply()
    }

    fun mapFilesExist(): Boolean {
        val essential = listOf("graph.json", "campus_updated.geojson")
        return essential.all { File(application.filesDir, it).exists() }
    }
}
