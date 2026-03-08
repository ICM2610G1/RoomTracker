package com.example.roomtracker.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.roomtracker.ui.theme.PrimaryOrange

@Composable
fun PlaceItemCard(
    title: String,
    onCardClick: () -> Unit,
    onInfoClick: () -> Unit
){

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🏢 Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = PrimaryOrange.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryOrange
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 🏷 Información
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Facultad de Artes",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 📏 Distancia
            Text(
                text = "200m",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(end = 8.dp)
            )

            // 🔵 BOTÓN AZUL
            FloatingActionButton(
                onClick = onInfoClick,
                containerColor = Color(0xFF2962FF),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}