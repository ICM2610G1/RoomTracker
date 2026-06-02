package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.model.ForoMensajeUi
import com.example.roomtracker.viewmodel.ForoViewModel

@Composable
fun ForoScreen(
    onBack: () -> Unit,
    viewModel: ForoViewModel
) {
    val state          by viewModel.state.collectAsStateWithLifecycle()
    val publicarState  by viewModel.publicarState.collectAsStateWithLifecycle()
    var texto          by remember { mutableStateOf("") }
    val listState      = rememberLazyListState()

    // Resetear campo tras publicar con éxito
    LaunchedEffect(publicarState) {
        if (publicarState is ForoViewModel.PublicarState.Success) {
            texto = ""
            viewModel.resetPublicarState()
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // ─── Header ──────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(40.dp))
            ScreenHeader(title = "Foro Campus", onBack = onBack)
            Spacer(Modifier.height(4.dp))
            Text(
                "Comparte una petición o necesidad · 1 mensaje por día",
                fontSize = 12.sp,
                color    = LightText
            )
            Spacer(Modifier.height(16.dp))
        }

        // ─── Contenido ───────────────────────────────────────────────────────
        when (val s = state) {

            is ForoViewModel.ForoState.Loading ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryOrange)
                }

            is ForoViewModel.ForoState.Error ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, color = Color.Gray, fontSize = 13.sp,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadMensajes() }) { Text("Reintentar") }
                    }
                }

            is ForoViewModel.ForoState.Success -> {
                val yaPublicoHoy = s.yaPublicoHoy
                val mensajes     = s.mensajes

                // Lista de mensajes
                LazyColumn(
                    state           = listState,
                    modifier        = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (mensajes.isEmpty()) {
                        item {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Forum, null,
                                        tint     = Color.LightGray,
                                        modifier = Modifier.size(56.dp))
                                    Spacer(Modifier.height(12.dp))
                                    Text("Sé el primero en publicar",
                                        color = LightText, fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        items(mensajes, key = { it.id }) { msg ->
                            ForoCard(msg)
                        }
                    }
                }

                // ─── Área de publicación ──────────────────────────────────────
                Surface(
                    color     = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                        if (yaPublicoHoy) {
                            // Ya publicó hoy
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        PrimaryOrange.copy(alpha = 0.08f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null,
                                    tint = PrimaryOrange, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Ya publicaste tu mensaje de hoy. Vuelve mañana.",
                                    fontSize = 13.sp,
                                    color    = PrimaryOrange,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            // Input de publicación
                            OutlinedTextField(
                                value         = texto,
                                onValueChange = { if (it.length <= 280) texto = it },
                                modifier      = Modifier.fillMaxWidth(),
                                placeholder   = { Text("¿Qué necesitas o propones?") },
                                minLines      = 2,
                                maxLines      = 4,
                                shape         = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor      = PrimaryOrange,
                                    unfocusedBorderColor    = Color(0xFFE0E0E0),
                                    focusedContainerColor   = Color.White,
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                trailingIcon = {
                                    Text(
                                        "${texto.length}/280",
                                        fontSize = 10.sp,
                                        color    = if (texto.length > 250) Color(0xFFE53935) else LightText,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            )

                            // Error al publicar
                            if (publicarState is ForoViewModel.PublicarState.Error) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    (publicarState as ForoViewModel.PublicarState.Error).message,
                                    color    = Color(0xFFE53935),
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Button(
                                onClick  = { viewModel.publicar(texto) },
                                enabled  = texto.isNotBlank() &&
                                           publicarState !is ForoViewModel.PublicarState.Loading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape  = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                            ) {
                                if (publicarState is ForoViewModel.PublicarState.Loading) {
                                    CircularProgressIndicator(
                                        color    = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Send, null,
                                        modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Publicar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

// ─── Card de mensaje ──────────────────────────────────────────────────────────

@Composable
private fun ForoCard(msg: ForoMensajeUi) {
    val accentColor = if (msg.esMio) PrimaryOrange else Color(0xFF1565C0)

    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            // Avatar inicial
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    msg.nombre.firstOrNull()?.uppercase() ?: "?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 16.sp,
                    color      = accentColor
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Nombre + "Tú" badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        msg.nombre.ifBlank { "Usuario" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 13.sp,
                        color      = DarkText
                    )
                    if (msg.esMio) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = PrimaryOrange.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Tú",
                                fontSize   = 9.sp,
                                color      = PrimaryOrange,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(msg.fechaFormateada, fontSize = 10.sp, color = LightText)
                }

                // Email
                Text(
                    msg.email,
                    fontSize = 10.sp,
                    color    = LightText.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(6.dp))

                // Contenido
                Text(
                    msg.contenido,
                    fontSize   = 14.sp,
                    color      = DarkText,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
