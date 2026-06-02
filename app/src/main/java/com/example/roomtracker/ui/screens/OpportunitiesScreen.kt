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
import com.example.roomtracker.model.OportunidadJson
import com.example.roomtracker.viewmodel.OpportunitiesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunitiesScreen(
    onBack: () -> Unit,
    onStartRoute: (nodeId: String, name: String) -> Unit = { _, _ -> },
    viewModel: OpportunitiesViewModel
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
        ScreenHeader(title = "Oportunidades", onBack = onBack)
        Spacer(modifier = Modifier.height(20.dp))

        when (val s = state) {

            is OpportunitiesViewModel.OpportunitiesState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryOrange)
                }
            }

            is OpportunitiesViewModel.OpportunitiesState.Error -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, color = Color.Gray, fontSize = 13.sp,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadOpportunities() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is OpportunitiesViewModel.OpportunitiesState.Success -> {
                val allOps = s.oportunidades
                val filters = listOf("Todos") + allOps.map { it.tipo }.distinct().sorted()
                val filtered = if (selectedFilter == "Todos") allOps
                               else allOps.filter { it.tipo == selectedFilter }

                // Chips de filtro
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                // Contador
                val disponibles = filtered.count { it.disponible }
                Text(
                    "${disponibles} DISPONIBLES · ${filtered.size - disponibles} CERRADAS",
                    fontWeight = FontWeight.Bold,
                    color      = LightText,
                    fontSize   = 11.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding      = PaddingValues(bottom = 24.dp)
                ) {
                    items(filtered, key = { it.id }) { op ->
                        OpportunityCard(op, onStartRoute)
                    }
                }
            }
        }
    }
}

@Composable
fun OpportunityCard(
    op: OportunidadJson,
    onStartRoute: (nodeId: String, name: String) -> Unit = { _, _ -> }
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Icono y color según tipo
    val (icon, tipoColor) = when (op.tipo) {
        "Monitoria"  -> Icons.Default.School        to Color(0xFF1565C0)
        "Vacante"    -> Icons.Default.Work           to Color(0xFF2E7D32)
        "Semillero"  -> Icons.Default.Science        to Color(0xFF6A1B9A)
        "Misión"     -> Icons.Default.Favorite       to Color(0xFFB71C1C)
        else         -> Icons.Default.BusinessCenter to PrimaryOrange
    }

    Card(
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ícono tipo
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(tipoColor.copy(alpha = 0.10f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tipoColor, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(op.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        color = DarkText, lineHeight = 19.sp)

                    Spacer(Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        // Badge tipo
                        Surface(
                            color = tipoColor.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                op.tipo,
                                color      = tipoColor,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        // Disponibilidad
                        if (!op.disponible) {
                            Surface(
                                color = Color(0xFFEEEEEE),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "CERRADA",
                                    color      = Color.Gray,
                                    fontSize   = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Fechas
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null,
                        tint = LightText, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Publicado: ${formatOpDate(op.fechaPublicacion)}",
                        fontSize = 11.sp, color = LightText
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventBusy, null,
                        tint = if (op.disponible) LightText else Color(0xFFE53935),
                        modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Cierre: ${formatOpDate(op.fechaCierre)}",
                        fontSize   = 11.sp,
                        color      = if (op.disponible) LightText else Color(0xFFE53935),
                        fontWeight = if (!op.disponible) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Detalle expandible
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Divider(color = Color(0xFFF0F0F0))
                Spacer(Modifier.height(12.dp))

                Text("Descripción", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DarkText)
                Spacer(Modifier.height(4.dp))
                Text(op.descripcion, fontSize = 13.sp, color = DarkText, lineHeight = 19.sp)

                if (op.requisitos.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text("Requisitos", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DarkText)
                    Spacer(Modifier.height(4.dp))
                    Text(op.requisitos, fontSize = 13.sp, color = LightText, lineHeight = 19.sp)
                }

                // Lugar con chip EN MAPA si tiene nodo
                if (op.lugar.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null,
                            tint = if (op.idNodo != null) tipoColor else LightText,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(op.lugar, fontSize = 12.sp, color = LightText)
                        if (op.idNodo != null) {
                            Spacer(Modifier.width(6.dp))
                            Surface(color = tipoColor.copy(alpha = 0.10f), shape = RoundedCornerShape(4.dp)) {
                                Text("EN MAPA", fontSize = 8.sp, color = tipoColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Fila de botones
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Botón IR AL LUGAR — solo si tiene nodo
                    if (op.idNodo != null) {
                        Button(
                            onClick = { onStartRoute(op.idNodo, op.lugar.ifBlank { op.titulo }) },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = tipoColor)
                        ) {
                            Icon(Icons.Default.Navigation, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("IR AL LUGAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (op.disponible && op.urlInscripcion.isNotBlank()) {
                            Button(
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(op.urlInscripcion))
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                            ) {
                                Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("INSCRIBIRSE", fontSize = 12.sp)
                            }
                        }
                        OutlinedButton(
                            onClick  = { expanded = false },
                            modifier = if (op.disponible && op.urlInscripcion.isNotBlank())
                                Modifier.weight(0.5f) else Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp)
                        ) { Text("CERRAR", fontSize = 12.sp) }
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick  = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = tipoColor),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        // tint the border to match tipo color (uses default otherwise)
                    )
                ) {
                    Text("VER DETALLES", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Helper de fecha ──────────────────────────────────────────────────────────

private fun formatOpDate(raw: String): String = try {
    val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val output = SimpleDateFormat("dd MMM yyyy", Locale("es", "CO"))
    output.format(input.parse(raw)!!).uppercase()
} catch (_: Exception) { raw }
