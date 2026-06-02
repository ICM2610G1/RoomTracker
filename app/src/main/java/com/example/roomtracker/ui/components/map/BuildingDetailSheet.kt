package com.example.roomtracker.ui.components.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.roomtracker.map.EdificioDetalle
import com.example.roomtracker.map.MapPoi
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange

// ─── Colores por tipo de espacio ─────────────────────────────────────────────

private fun espacioColor(tipo: String): Color = when (tipo) {
    "laboratorio" -> Color(0xFF1565C0)
    "aula"        -> Color(0xFF2E7D32)
    "coworking"   -> Color(0xFF00838F)
    "oficina"     -> Color(0xFF6A1B9A)
    "servicio"    -> Color(0xFF546E7A)
    else          -> Color(0xFF757575)
}

private fun espacioLabel(tipo: String): String = when (tipo) {
    "laboratorio" -> "Lab"
    "aula"        -> "Aula"
    "coworking"   -> "CW"
    "oficina"     -> "Ofic."
    "servicio"    -> "Serv."
    else          -> "Otro"
}

// ─── Componente principal ────────────────────────────────────────────────────

@Composable
fun BuildingDetailSheet(
    poi: MapPoi,
    edificioDetalle: EdificioDetalle?,
    onClose: () -> Unit,
    onStartRoute: () -> Unit
) {
    val tipo      = poi.info?.tipo
    val icon      = tipoIcon(tipo)
    val color     = tipoColor(tipo)
    val tipoLabel = when (tipo) {
        "cafeteria"   -> "Cafetería"
        "restaurante" -> "Restaurante"
        "cancha"      -> "Espacio Deportivo"
        "auditorio"   -> "Auditorio"
        "biblioteca"  -> "Biblioteca"
        "capilla"     -> "Capilla / Iglesia"
        "enfermeria"  -> "Enfermería / Hospital"
        "parqueadero" -> "Parqueadero"
        "agua"        -> "Punto de Agua"
        "bus"         -> "Parada de Bus"
        "edificio"    -> "Edificio Académico"
        null          -> "Punto de Interés"
        else          -> tipo.replaceFirstChar { it.uppercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // ─── Header ──────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier         = Modifier
                        .size(52.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        poi.displayName,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = DarkText
                    )
                    Text(
                        "nodo: ${poi.nodeId}",
                        fontSize = 9.sp,
                        color    = LightText.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
        }

        // Badge de tipo
        Row(modifier = Modifier.padding(horizontal = 20.dp)) {
            Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                Text(
                    tipoLabel,
                    color      = color,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── Imagen del edificio ──────────────────────────────────────────────
        val imageUrl = edificioDetalle?.imagenUrl
        if (imageUrl != null) {
            AsyncImage(
                model             = imageUrl,
                contentDescription = poi.displayName,
                contentScale      = ContentScale.Crop,
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        } else {
            // Placeholder cuando no hay imagen
            Card(
                shape    = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ─── Descripción ──────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "SOBRE ESTE LUGAR",
                style      = MaterialTheme.typography.labelMedium,
                color      = LightText,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                edificioDetalle?.descripcion
                    ?: poi.info?.descripcion
                    ?: "Punto de interés en el campus de la Pontificia Universidad Javeriana Bogotá.",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkText
            )
        }

        // ─── Pisos (solo si hay detalle de edificio) ───────────────────────
        if (edificioDetalle != null) {
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Apartment, null, tint = LightText, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${edificioDetalle.pisos.size} PISOS  ·  ESPACIOS POR PISO",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = LightText,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            // Leyenda de colores
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "laboratorio" to "Laboratorio",
                        "aula"        to "Aula / Sala",
                        "coworking"   to "Coworking"
                    ).forEach { (t, l) ->
                        LeyendaChip(t, l)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "oficina"  to "Oficina",
                        "servicio" to "Servicio"
                    ).forEach { (t, l) ->
                        LeyendaChip(t, l)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            edificioDetalle.pisos.forEach { piso ->
                PisoSection(piso = piso, accentColor = color)
                Spacer(Modifier.height(4.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // ─── Botón de ruta ────────────────────────────────────────────────────
        Button(
            onClick  = onStartRoute,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(horizontal = 20.dp),
            shape  = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Icon(Icons.Default.Navigation, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Iniciar Ruta", fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Sección de piso expandible ───────────────────────────────────────────────

@Composable
private fun PisoSection(piso: com.example.roomtracker.map.PisoDetalle, accentColor: Color) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        // Cabecera del piso (siempre visible)
        Surface(
            modifier  = Modifier
                .fillMaxWidth()
                .clip(if (expanded) RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                      else RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded },
            color     = if (expanded) accentColor else Color(0xFFF5F5F5)
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge número de piso
                    Box(
                        modifier         = Modifier
                            .background(
                                if (expanded) Color.White.copy(alpha = 0.25f) else accentColor.copy(alpha = 0.12f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            piso.numero,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 13.sp,
                            color      = if (expanded) Color.White else accentColor
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            piso.nombre,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            color      = if (expanded) Color.White else DarkText
                        )
                        Text(
                            "${piso.espacios.size} espacio(s)",
                            fontSize = 11.sp,
                            color    = if (expanded) Color.White.copy(alpha = 0.7f) else LightText
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (expanded) Color.White else LightText
                )
            }
        }

        // Contenido expandible
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Surface(
                color = Color(0xFFFAFAFA),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Servicios del piso
                    if (piso.servicios.isNotEmpty()) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            piso.servicios.forEach { svc ->
                                Surface(
                                    color = PrimaryOrange.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "☕ $svc",
                                        fontSize   = 10.sp,
                                        color      = PrimaryOrange,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    // Grid de chips por espacio
                    // Usamos un LazyVerticalGrid con altura fija proporcional al contenido
                    val rows = (piso.espacios.size + 2) / 3  // 3 columnas
                    val gridHeight = (rows * 60).dp
                    LazyVerticalGrid(
                        columns              = GridCells.Fixed(3),
                        modifier             = Modifier
                            .fillMaxWidth()
                            .heightIn(max = gridHeight + 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement   = Arrangement.spacedBy(6.dp),
                        userScrollEnabled     = false
                    ) {
                        items(piso.espacios) { espacio ->
                            EspacioChip(espacio)
                        }
                    }
                }
            }
        }
    }
}

// ─── Chip de espacio/salón ────────────────────────────────────────────────────

@Composable
private fun EspacioChip(espacio: com.example.roomtracker.map.EspacioDetalle) {
    val c = espacioColor(espacio.tipo)

    Surface(
        shape  = RoundedCornerShape(10.dp),
        color  = c.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Código grande
            Text(
                espacio.codigo,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 15.sp,
                color      = c
            )
            // Nombre pequeño
            Text(
                espacio.nombre,
                fontSize   = 9.sp,
                color      = DarkText,
                lineHeight = 11.sp,
                maxLines   = 2
            )
            Spacer(Modifier.height(4.dp))
            // Badge mini de tipo
            Surface(
                color = c.copy(alpha = 0.15f),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(
                    espacioLabel(espacio.tipo),
                    fontSize   = 7.sp,
                    color      = c,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

// ─── Chip de leyenda ─────────────────────────────────────────────────────────

@Composable
private fun LeyendaChip(tipo: String, label: String) {
    val c = espacioColor(tipo)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(c, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = LightText)
    }
}
