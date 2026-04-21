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
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange

data class LocationRequest(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val initial: String
)

@Composable
fun LocationRequestsScreen(
    onBack: () -> Unit
) {
    var requests by remember {
        mutableStateOf(
            listOf(
                LocationRequest("1", "Mariana López", "m.lopez@javeriana.edu.co", "ESTUDIANTE", "M"),
                LocationRequest("2", "Roberto Gomez", "r.gomez@javeriana.edu.co", "PROFESOR", "R"),
                LocationRequest("3", "Lucía Fernández", "l.fer@javeriana.edu.co", "ESTUDIANTE", "L")
            )
        )
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

        if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes solicitudes pendientes", color = LightText)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(requests) { request ->
                    RequestCard(
                        request = request,
                        onAccept = { requests = requests.filter { it.id != request.id } },
                        onDecline = { requests = requests.filter { it.id != request.id } }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    request: LocationRequest,
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
                        request.initial,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryOrange,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(request.name, fontWeight = FontWeight.Bold, color = DarkText)
                    Text(request.email, fontSize = 12.sp, color = LightText)
                    Text(request.role, fontSize = 11.sp, color = PrimaryOrange, fontWeight = FontWeight.SemiBold)
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
