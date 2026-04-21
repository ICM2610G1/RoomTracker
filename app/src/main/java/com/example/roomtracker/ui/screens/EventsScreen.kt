package com.example.roomtracker.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.google.android.gms.maps.model.LatLng

data class CampusEvent(
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val location: String,
    val description: String,
    val link: String? = null,
    val poiLocation: LatLng? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onBack: () -> Unit,
    onStartRoute: (LatLng, String) -> Unit = { _, _ -> }
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Tutorias", "Conciertos", "Clases", "Juegos")

    val events = listOf(
        CampusEvent("Clasificación Fútbol", "Juegos", "Hoy", "2:00 PM", "Cancha de Fútbol", "Clasificatorios internos", "https://forms.gle/javeriana-futbol", LatLng(4.628, -74.064)),
        CampusEvent("Concierto Filarmónica", "Conciertos", "Mañana", "6:00 PM", "Auditorio Felix Restrepo", "Música clásica en vivo", "https://bit.ly/concierto-jav", LatLng(4.627, -74.065)),
        CampusEvent("Monitoría Cálculo I", "Tutorias", "25 Ene", "9:00 AM", "Edificio 67 - Sala 402", "Dudas sobre derivadas", null, LatLng(4.629, -74.063)),
        CampusEvent("Taller de Dibujo", "Clases", "26 Ene", "11:00 AM", "Centro Cultural", "Traer carboncillo", "https://bit.ly/taller-arte", LatLng(4.626, -74.066))
    )

    val filteredEvents = if (selectedFilter == "Todos") events else events.filter { it.category == selectedFilter }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        ScreenHeader(title = "Eventos y Noticias", onBack = onBack)

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryOrange,
                        selectedLabelColor = Color.White,
                        labelColor = LightText
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("CALENDARIO DE EVENTOS", fontWeight = FontWeight.Bold, color = LightText, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(filteredEvents) { event ->
                EventCard(event, onStartRoute)
            }
        }
    }
}

@Composable
fun EventCard(event: CampusEvent, onStartRoute: (LatLng, String) -> Unit) {
    var showDetails by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = PrimaryOrange.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        event.category,
                        color = PrimaryOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(event.date, color = LightText, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkText)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(event.location, fontSize = 14.sp, color = LightText)
            }

            if (showDetails) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(event.description, fontSize = 14.sp, color = DarkText)
                
                if (event.link != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.link))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryOrange)
                    ) {
                        Icon(Icons.Default.Link, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FORMULARIO DE INSCRIPCIÓN")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            if (event.poiLocation != null) {
                                onStartRoute(event.poiLocation, event.location)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Icon(Icons.Default.Navigation, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COMENZAR VIAJE", fontSize = 12.sp)
                    }
                    
                    OutlinedButton(
                        onClick = { showDetails = false },
                        modifier = Modifier.weight(0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CERRAR")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showDetails = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text("RESERVAR / VER MÁS")
                }
            }
        }
    }
}
