package com.example.roomtracker.ui.components.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp



@Composable
fun BuildingDetailSheet(
    buildingName: String,
    onClose: () -> Unit,
    onStartRoute: () -> Unit
){

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // HEADER
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = buildingName,
                    style = MaterialTheme.typography.titleLarge
                )
                Text("Facultad de Artes • 200m")
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // IMAGEN GRANDE (fake)
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Imagen del Edificio")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SOBRE EL LUGAR",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Edificio académico con múltiples salones, oficinas administrativas y laboratorios especializados.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔥 PISOS
        FloorDetail(
            floor = "Piso 1",
            services = listOf("Baños", "Cafetería", "Oficina administrativa"),
            rooms = listOf("101", "102", "103", "104")
        )

        FloorDetail(
            floor = "Piso 2",
            services = listOf("Baños", "Sala de estudio"),
            rooms = listOf("201", "202", "203")
        )

        FloorDetail(
            floor = "Piso 3",
            services = listOf("Laboratorio de redes"),
            rooms = listOf("301", "302")
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { onStartRoute() },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(30.dp)
        ) {

            Icon(Icons.Default.Navigation, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Ruta")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
