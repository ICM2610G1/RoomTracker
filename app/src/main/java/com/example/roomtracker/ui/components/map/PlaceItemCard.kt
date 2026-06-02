package com.example.roomtracker.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.map.MapPoi
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange

// ─── Icono y color por TipoEdificio ──────────────────────────────────────────

fun tipoIcon(tipo: String?): ImageVector = when (tipo) {
    "cafeteria"    -> Icons.Default.LocalCafe
    "restaurante"  -> Icons.Default.Restaurant
    "cancha"       -> Icons.Default.FitnessCenter
    "auditorio"    -> Icons.Default.Theaters
    "biblioteca"   -> Icons.Default.LocalLibrary
    "capilla"      -> Icons.Default.Church
    "enfermeria"   -> Icons.Default.LocalHospital
    "parqueadero"  -> Icons.Default.LocalParking
    "agua"         -> Icons.Default.WaterDrop
    "bus"          -> Icons.Default.DirectionsBus
    "tienda"       -> Icons.Default.ShoppingBag
    else           -> Icons.Default.Business          // "edificio" y null
}

fun tipoColor(tipo: String?): Color = when (tipo) {
    "cafeteria"    -> Color(0xFF6D4C41)
    "restaurante"  -> Color(0xFFE65100)
    "cancha"       -> Color(0xFF2E7D32)
    "auditorio"    -> Color(0xFF6A1B9A)
    "biblioteca"   -> Color(0xFF1565C0)
    "capilla"      -> Color(0xFF880E4F)
    "enfermeria"   -> Color(0xFFC62828)
    "parqueadero"  -> Color(0xFF37474F)
    "agua"         -> Color(0xFF0288D1)
    "bus"          -> Color(0xFF00695C)
    "tienda"       -> Color(0xFFE65100)
    else           -> PrimaryOrange
}

// ─── Card ─────────────────────────────────────────────────────────────────────

@Composable
fun PlaceItemCard(
    poi: MapPoi,
    onCardClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val tipo  = poi.info?.tipo
    val icon  = tipoIcon(tipo)
    val color = tipoColor(tipo)

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono con color según tipo
            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre + nodeId pequeño
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = poi.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = DarkText,
                    maxLines   = 1
                )
                // nodeId muy pequeño — útil para identificar nodos sin nombre
                Text(
                    text     = poi.nodeId,
                    fontSize = 9.sp,
                    color    = LightText.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            // Botón de info
            FloatingActionButton(
                onClick         = onInfoClick,
                containerColor  = color,
                modifier        = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector      = Icons.Default.Info,
                    contentDescription = null,
                    tint             = Color.White,
                    modifier         = Modifier.size(18.dp)
                )
            }
        }
    }
}
