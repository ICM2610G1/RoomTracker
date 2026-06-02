package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.model.ConversacionEstudiante
import com.example.roomtracker.viewmodel.OperadorViewModel

@Composable
fun OperadorHomeScreen(
    viewModel: OperadorViewModel,
    onOpenChat: (chatId: String, studentName: String) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // ─── Header operador ──────────────────────────────────────────────────
        Surface(
            color     = PrimaryOrange,
            shadowElevation = 4.dp,
            modifier  = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SupportAgent, null,
                        tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Panel de Operador",
                        color      = Color.White.copy(alpha = 0.80f),
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Medium)
                    Text(viewModel.operadorNombre,
                        color      = Color.White,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.Logout, null, tint = Color.White)
                }
            }
        }

        // ─── Contador ─────────────────────────────────────────────────────────
        if (state is OperadorViewModel.OperadorState.Success) {
            val count = (state as OperadorViewModel.OperadorState.Success).conversaciones.size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$count CONVERSACION${if (count != 1) "ES" else ""}",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = LightText,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.loadConversaciones() }) {
                    Icon(Icons.Default.Refresh, null, tint = LightText)
                }
            }
        }

        // ─── Contenido ────────────────────────────────────────────────────────
        when (val s = state) {
            is OperadorViewModel.OperadorState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryOrange)
                        Spacer(Modifier.height(12.dp))
                        Text("Cargando conversaciones...", color = LightText, fontSize = 13.sp)
                    }
                }

            is OperadorViewModel.OperadorState.Error ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, color = Color.Gray, fontSize = 13.sp,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadConversaciones() }) {
                            Text("Reintentar")
                        }
                    }
                }

            is OperadorViewModel.OperadorState.Success -> {
                if (s.conversaciones.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(Icons.Default.Inbox, null,
                                tint     = Color.LightGray,
                                modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Sin conversaciones aún",
                                color = LightText, fontSize = 16.sp,
                                fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text("Los estudiantes verán tu servicio\nen el menú de la app",
                                color     = LightText,
                                fontSize  = 13.sp,
                                textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.conversaciones, key = { it.idChat }) { conv ->
                            ConversacionCard(conv) {
                                onOpenChat(conv.idChat, conv.nombreEstudiante)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversacionCard(
    conv: ConversacionEstudiante,
    onClick: () -> Unit
) {
    Card(
        onClick    = onClick,
        shape      = RoundedCornerShape(16.dp),
        colors     = CardDefaults.cardColors(containerColor = Color.White),
        elevation  = CardDefaults.cardElevation(2.dp),
        modifier   = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(PrimaryOrange.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    conv.nombreEstudiante.firstOrNull()?.uppercase() ?: "?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 18.sp,
                    color      = PrimaryOrange
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    conv.nombreEstudiante.ifBlank { "Estudiante" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = DarkText
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    conv.lastMessage,
                    fontSize  = 12.sp,
                    color     = LightText,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(conv.lastTime, fontSize = 10.sp, color = LightText)
                Spacer(Modifier.height(6.dp))
                Icon(Icons.Default.ChevronRight, null, tint = LightText)
            }
        }
    }
}
