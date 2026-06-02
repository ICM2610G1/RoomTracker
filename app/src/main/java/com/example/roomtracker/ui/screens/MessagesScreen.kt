package com.example.roomtracker.ui.screens

import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.components.common.SearchField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.roomtracker.navigation.AppScreens
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.viewmodel.ChatViewModel
import com.example.roomtracker.model.ConversacionServicio

@Composable
fun MessagesScreen(
    navController: NavController,
    onBack: () -> Unit,
    chatViewModel: ChatViewModel
) {
    val conversaciones by chatViewModel.conversaciones.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()

    var search by remember { mutableStateOf("") }

    val filtradas = if (search.isBlank()) conversaciones
    else conversaciones.filter {
        it.operador.nombreCompleto.contains(search, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        ScreenHeader(
            title  = "Mensajes",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(24.dp))

        SearchField(
            value = search,
            onValueChange = { search = it },
            placeholder = "Buscar conversación..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryOrange)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filtradas, key = { it.operador.idUsuario }) { conversacion ->
                    ConversationItem(
                        conversacion = conversacion,
                        onClick = {
                            navController.navigate(
                                AppScreens.Chat.name + "/${conversacion.operador.idUsuario}/${conversacion.operador.nombreCompleto}"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversacion: ConversacionServicio,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    conversacion.operador.nombre.first().uppercaseChar().toString(),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryOrange,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversacion.operador.nombreCompleto,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversacion.lastTime.isNotEmpty()) {
                        Text(
                            text = conversacion.lastTime,
                            fontSize = 12.sp,
                            color = LightText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conversacion.lastMessage,
                    color = LightText,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            if (conversacion.unread) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(PrimaryOrange, shape = RoundedCornerShape(50))
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LightText
            )
        }
    }
}
