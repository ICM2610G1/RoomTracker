package com.example.roomtracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.viewmodel.ChatViewModel
import com.example.roomtracker.model.MensajeItem
import java.io.File

@Composable
fun ChatScreen(
    operadorId: String,
    title: String,
    onBack: () -> Unit,
    chatViewModel: ChatViewModel,
    chatIdDirect: String = ""   // si se pasa, el operador abre este chat directamente
) {
    val mensajes     by chatViewModel.mensajes.collectAsStateWithLifecycle()
    val currentChatId by chatViewModel.currentChatId.collectAsStateWithLifecycle()
    val isLoading    by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by chatViewModel.errorMessage.collectAsStateWithLifecycle()

    val context   = LocalContext.current
    var message   by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val chatId = currentChatId
            if (chatId != null) chatViewModel.sendImageMessage(chatId, it)
        }
    }

    // Cámara — URI temporal en cache
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val chatId = currentChatId
            val uri    = cameraUri
            if (chatId != null && uri != null) chatViewModel.sendImageMessage(chatId, uri)
        }
    }

    // Modo estudiante: abrir o crear chat con el operador
    // Modo operador: abrir chat existente directamente por chatIdDirect
    LaunchedEffect(operadorId, chatIdDirect) {
        chatViewModel.clearMessages()
        if (chatIdDirect.isNotBlank()) {
            chatViewModel.setChatDirect(chatIdDirect)
        } else {
            chatViewModel.openOrCreateChat(operadorId) { /* chatId listo */ }
        }
    }

    // Scroll al último mensaje cuando llegan nuevos
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) listState.animateScrollToItem(mensajes.size - 1)
    }

    // Limpiar error tras mostrarlo
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(4000)
            chatViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .statusBarsPadding()
    ) {

        ScreenHeader(
            title = title,
            onBack = {
                chatViewModel.clearMessages()
                onBack()
            }
        )

        // ── Banner de error ────────────────────────────────────────────────
        if (errorMessage != null) {
            Surface(
                color    = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, null,
                        tint     = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        errorMessage ?: "",
                        color    = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { chatViewModel.clearError() },
                        modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null,
                            tint     = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Loading / empty state / mensajes ──────────────────────────────
        when {
            isLoading && mensajes.isEmpty() -> {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryOrange)
                }
            }

            mensajes.isEmpty() -> {
                Box(
                    modifier           = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentAlignment   = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint     = LightText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("¿En qué podemos ayudarte?", color = LightText, fontSize = 15.sp)
                        Text("Escribe tu mensaje para comenzar.", color = LightText, fontSize = 13.sp)
                    }
                }
            }

            else -> {
                LazyColumn(
                    state          = listState,
                    modifier       = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp, top = 4.dp)
                ) {
                    items(mensajes, key = { it.idMensaje }) { msg ->
                        ChatBubble(msg)
                    }
                }
            }
        }

        // ── Input ─────────────────────────────────────────────────────────
        Surface(
            color           = Color.White,
            shadowElevation = 8.dp,
            modifier        = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Galería
                FilledTonalIconButton(
                    onClick  = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.size(42.dp),
                    colors   = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = PrimaryOrange.copy(alpha = 0.12f)
                    )
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Galería",
                        tint = PrimaryOrange, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(4.dp))

                // Cámara
                FilledTonalIconButton(
                    onClick = {
                        val tmpFile = File(context.cacheDir,
                            "chat_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", tmpFile)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.size(42.dp),
                    colors   = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = PrimaryOrange.copy(alpha = 0.12f)
                    )
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Cámara",
                        tint = PrimaryOrange, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(4.dp))

                OutlinedTextField(
                    value         = message,
                    onValueChange = { message = it },
                    placeholder   = { Text("Escribe un mensaje...", fontSize = 14.sp) },
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(24.dp),
                    maxLines      = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Botón enviar
                val canSend = message.isNotBlank() && currentChatId != null
                FilledIconButton(
                    onClick = {
                        val chatId = currentChatId
                        if (canSend && chatId != null) {
                            chatViewModel.sendMessage(chatId, message)
                            message = ""
                        }
                    },
                    colors  = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (canSend) PrimaryOrange else Color.LightGray
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar",
                        modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: MensajeItem) {
    Row(
        modifier             = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.esMio) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape  = RoundedCornerShape(
                topStart    = 16.dp,
                topEnd      = 16.dp,
                bottomStart = if (message.esMio) 16.dp else 4.dp,
                bottomEnd   = if (message.esMio) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.esMio) PrimaryOrange
                                 else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = if (message.tipo == "imagen") 4.dp else 12.dp,
                    vertical   = if (message.tipo == "imagen") 4.dp else 8.dp
                )
            ) {
                if (message.tipo == "imagen") {
                    AsyncImage(
                        model             = message.contenidoTexto,
                        contentDescription = "Imagen",
                        contentScale      = ContentScale.Crop,
                        modifier          = Modifier
                            .widthIn(min = 160.dp, max = 260.dp)
                            .heightIn(max = 220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Text(
                        text  = message.contenidoTexto,
                        color = if (message.esMio) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text     = message.fechaEnvio,
                    fontSize = 10.sp,
                    color    = if (message.esMio) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.70f)
                               else LightText,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
