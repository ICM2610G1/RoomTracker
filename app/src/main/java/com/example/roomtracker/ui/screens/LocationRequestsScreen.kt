package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.model.AmigoConAmistad
import com.example.roomtracker.viewmodel.FriendsViewModel

@Composable
fun LocationRequestsScreen(
    onBack: () -> Unit,
    viewModel: FriendsViewModel
) {
    val pendingRequests by viewModel.pendingRequests.collectAsStateWithLifecycle()

    // Recargar solicitudes cada vez que se abre esta pantalla
    LaunchedEffect(Unit) {
        viewModel.loadPendingRequests()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        ScreenHeader(
            title = "Solicitudes",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "PENDIENTES POR APROBAR",
            fontSize = 12.sp,
            color = LightText,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (pendingRequests.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = null,
                        tint = LightText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No tienes solicitudes pendientes", color = LightText)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(pendingRequests, key = { it.amistadId }) { request ->
                    RequestCard(
                        amigoConAmistad = request,
                        onAccept = { viewModel.acceptRequest(request.amistadId) },
                        onDecline = { viewModel.rejectRequest(request.amistadId) }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    amigoConAmistad: AmigoConAmistad,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PrimaryOrange.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        amigoConAmistad.usuario.nombre.first().uppercaseChar().toString(),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryOrange,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${amigoConAmistad.usuario.nombre} ${amigoConAmistad.usuario.apellido}",
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        "ESTUDIANTE JAVERIANO",
                        fontSize = 11.sp,
                        color = PrimaryOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Rechazar")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text("Aceptar")
                }
            }
        }
    }
}
