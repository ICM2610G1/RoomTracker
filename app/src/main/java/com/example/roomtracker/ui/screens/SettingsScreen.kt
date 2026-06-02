package com.example.roomtracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.roomtracker.ui.theme.PrimaryOrange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenStats: () -> Unit,
    authViewModel: AuthViewModel
) {
    val userProfile       by authViewModel.userProfile.collectAsStateWithLifecycle()
    val profileUpdateState by authViewModel.profileUpdateState.collectAsStateWithLifecycle()

    var editMode  by remember { mutableStateOf(false) }
    var nombre    by remember(userProfile) { mutableStateOf(userProfile?.nombre ?: "") }
    var apellido  by remember(userProfile) { mutableStateOf(userProfile?.apellido ?: "") }

    // Cerrar edición al guardar con éxito
    LaunchedEffect(profileUpdateState) {
        if (profileUpdateState is AuthViewModel.AuthState.Success) {
            editMode = false
            authViewModel.resetProfileUpdateState()
        }
    }

    LaunchedEffect(Unit) { authViewModel.loadUserProfile() }

    val isUploadingPhoto = profileUpdateState is AuthViewModel.AuthState.Loading && !editMode

    // Galería para foto de perfil
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { authViewModel.uploadProfilePhoto(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        ScreenHeader(title = "Ajustes", onBack = onBack)

        Spacer(modifier = Modifier.height(24.dp))

        // ── Tarjeta de usuario ──────────────────────────────────────────────
        Card(
            shape    = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Avatar con cámara overlay
                    Box(
                        modifier         = Modifier.size(72.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        if (userProfile?.fotoUrl != null) {
                            AsyncImage(
                                model             = userProfile!!.fotoUrl,
                                contentDescription = "Foto de perfil",
                                contentScale      = ContentScale.Crop,
                                modifier          = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, PrimaryOrange, CircleShape)
                                    .clickable { photoPickerLauncher.launch("image/*") }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryOrange.copy(alpha = 0.15f))
                                    .border(2.dp, PrimaryOrange.copy(alpha = 0.3f), CircleShape)
                                    .clickable { photoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = userProfile?.nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    fontWeight = FontWeight.Bold,
                                    color      = PrimaryOrange,
                                    fontSize   = 28.sp
                                )
                            }
                        }

                        // Ícono de cámara / spinner de carga
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(PrimaryOrange)
                                .clickable(enabled = !isUploadingPhoto) {
                                    photoPickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploadingPhoto) {
                                CircularProgressIndicator(
                                    color     = Color.White,
                                    modifier  = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.CameraAlt, null,
                                    tint = Color.White, modifier = Modifier.size(13.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = if (userProfile != null) "${userProfile!!.nombre} ${userProfile!!.apellido}" else "Cargando...",
                            fontWeight = FontWeight.SemiBold,
                            color      = DarkText,
                            fontSize   = 16.sp
                        )
                        Text(
                            text     = userProfile?.email ?: "",
                            color    = LightText,
                            fontSize = 13.sp
                        )
                    }

                    // Botón editar
                    IconButton(onClick = {
                        editMode = !editMode
                        if (!editMode) {
                            nombre   = userProfile?.nombre ?: ""
                            apellido = userProfile?.apellido ?: ""
                        }
                    }) {
                        Icon(
                            if (editMode) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (editMode) "Cancelar" else "Editar",
                            tint = if (editMode) LightText else PrimaryOrange
                        )
                    }
                }

                // Error de foto (fuera del modo edición)
                if (!editMode && profileUpdateState is AuthViewModel.AuthState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        (profileUpdateState as AuthViewModel.AuthState.Error).message,
                        color    = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                // ── Formulario de edición ───────────────────────────────
                if (editMode) {
                    Spacer(Modifier.height(20.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))

                    Text("EDITAR PERFIL", fontSize = 11.sp, color = LightText,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value         = nombre,
                        onValueChange = { nombre = it },
                        label         = { Text("Nombre") },
                        leadingIcon   = { Icon(Icons.Default.Person, null) },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        singleLine    = true
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value         = apellido,
                        onValueChange = { apellido = it },
                        label         = { Text("Apellido") },
                        leadingIcon   = { Icon(Icons.Default.Person, null) },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        singleLine    = true
                    )

                    Spacer(Modifier.height(16.dp))

                    val isSaving = profileUpdateState is AuthViewModel.AuthState.Loading
                    Button(
                        onClick  = { authViewModel.updateProfile(nombre.trim(), apellido.trim()) },
                        enabled  = !isSaving && nombre.isNotBlank() && apellido.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White,
                                modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (profileUpdateState is AuthViewModel.AuthState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            (profileUpdateState as AuthViewModel.AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error, fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Comunicación ────────────────────────────────────────────────────
        Text("COMUNICACIÓN", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            onClick = onOpenMessages,
            shape   = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ChatBubbleOutline, null,
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text("Chats de servicios", modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            onClick  = onOpenStats,
            shape    = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DirectionsWalk, null, tint = PrimaryOrange)
                Spacer(Modifier.width(16.dp))
                Text("Mis pasos en la Javeriana", modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Cerrar sesión ───────────────────────────────────────────────────
        Button(
            onClick  = onLogout,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                contentColor   = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }

        Spacer(Modifier.height(24.dp))
    }
}
