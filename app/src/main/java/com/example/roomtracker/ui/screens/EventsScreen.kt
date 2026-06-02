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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.model.EventoJson
import com.example.roomtracker.viewmodel.EventsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onBack: () -> Unit,
    // nodeId del mapa + nombre del lugar; solo se llama si el evento tiene id_nodo
    onStartRoute: (nodeId: String, name: String) -> Unit = { _, _ -> },
    viewModel: EventsViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("Todos") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        ScreenHeader(title = "Eventos y Noticias", onBack = onBack)
        Spacer(modifier = Modifier.height(20.dp))

        when (val s = state) {

            is EventsViewModel.EventsState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryOrange)
                }
            }

            is EventsViewModel.EventsState.Error -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, color = Color.Gray, fontSize = 13.sp,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadEvents() }) { Text("Reintentar") }
                    }
                }
            }

            is EventsViewModel.EventsState.Success -> {
                val allEventos = s.eventos
                val filters   = listOf("Todos") + allEventos.map { it.tipo }.distinct().sorted()
                val filtered  = if (selectedFilter == "Todos") allEventos
                                else allEventos.filter { it.tipo == selectedFilter }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick  = { selectedFilter = filter },
                            label    = { Text(filter, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryOrange,
                                selectedLabelColor     = Color.White,
                                labelColor             = LightText
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("CALENDARIO DE EVENTOS",
                    fontWeight = FontWeight.Bold, color = LightText, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding      = PaddingValues(bottom = 24.dp)
                ) {
                    items(filtered, key = { it.id }) { evento ->
                        EventCard(evento = evento, onStartRoute = onStartRoute)
                    }
                }
            }
        }
    }
}

// ─── Card de evento ───────────────────────────────────────────────────────────

@Composable
fun EventCard(
    evento: EventoJson,
    onStartRoute: (nodeId: String, name: String) -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val tipoColor = when (evento.tipo) {
        "Académico"   -> Color(0xFF1565C0)
        "Laboral"     -> Color(0xFF2E7D32)
        "Conferencia" -> Color(0xFF6A1B9A)
        "Deportivo"   -> Color(0xFFE65100)
        "Cultural"    -> Color(0xFFC62828)
        "Taller"      -> Color(0xFF00838F)
        else          -> PrimaryOrange
    }

    Card(
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado: tipo + fecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = tipoColor.copy(alpha = 0.10f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        evento.tipo, color = tipoColor,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (!evento.activo) {
                    Spacer(Modifier.width(8.dp))
                    Surface(color = Color(0xFFEEEEEE), shape = RoundedCornerShape(8.dp)) {
                        Text("FINALIZADO", color = Color.Gray,
                            fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(formatEventDate(evento.fechaInicio), color = LightText, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(evento.titulo, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DarkText)

            Spacer(modifier = Modifier.height(8.dp))

            // Lugar
            if (evento.lugar.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null,
                        tint = if (evento.idNodo != null) PrimaryOrange else LightText,
                        modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(evento.lugar, fontSize = 13.sp, color = LightText)
                    // Indicador de que tiene ubicación en el mapa
                    if (evento.idNodo != null) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = PrimaryOrange.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("EN MAPA", fontSize = 8.sp, color = PrimaryOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
            }

            // Hora
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null,
                    tint = LightText, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${formatEventTime(evento.fechaInicio)} – ${formatEventTime(evento.fechaFin)}",
                    fontSize = 12.sp, color = LightText
                )
            }

            // Sección expandida
            if (showDetails) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(12.dp))
                Text(evento.descripcion, fontSize = 14.sp, color = DarkText, lineHeight = 20.sp)

                if (evento.urlInscripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(evento.urlInscripcion))
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.textButtonColors(contentColor = PrimaryOrange)
                    ) {
                        Icon(Icons.Default.Link, null)
                        Spacer(Modifier.width(8.dp))
                        Text("FORMULARIO DE INSCRIPCIÓN")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Botón IR AL LUGAR — solo si tiene nodo asignado
                    if (evento.idNodo != null) {
                        Button(
                            onClick  = { onStartRoute(evento.idNodo, evento.lugar.ifBlank { evento.titulo }) },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                        ) {
                            Icon(Icons.Default.Navigation, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("IR AL LUGAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick  = { showDetails = false },
                        modifier = if (evento.idNodo != null) Modifier.weight(0.5f)
                                   else Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp)
                    ) { Text("CERRAR", fontSize = 12.sp) }
                }

            } else {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick  = { showDetails = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (evento.activo) PrimaryOrange else Color(0xFFBDBDBD)
                    )
                ) {
                    Text(if (evento.activo) "VER MÁS / INSCRIBIRSE" else "VER DETALLES")
                }
            }
        }
    }
}

// ─── Helpers de fecha ─────────────────────────────────────────────────────────

private fun formatEventDate(iso: String): String = try {
    val input  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val output = SimpleDateFormat("dd MMM", Locale("es", "CO"))
    output.format(input.parse(iso)!!).uppercase()
} catch (_: Exception) { iso }

private fun formatEventTime(iso: String): String = try {
    val input  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val output = SimpleDateFormat("h:mm a", Locale("es", "CO"))
    output.format(input.parse(iso)!!)
} catch (_: Exception) { "" }
