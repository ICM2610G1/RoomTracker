package com.example.roomtracker.ui.components.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.map.MapPoi
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange

@Composable
fun AccesoPicker(
    group: List<MapPoi>,
    onAccesoInfo: (MapPoi) -> Unit,
    onAccesoRoute: (MapPoi) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Text(
            "¿A qué acceso de ${group.first().displayName} quieres ir?",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = DarkText
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${group.size} entradas disponibles",
            fontSize = 12.sp,
            color    = LightText
        )
        Spacer(Modifier.height(16.dp))

        group.forEachIndexed { index, poi ->
            Card(
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Place, null,
                        tint     = PrimaryOrange,
                        modifier = Modifier.size(20.dp))

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Acceso ${index + 1}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            color      = DarkText
                        )
                        Text(poi.nodeId, fontSize = 9.sp, color = LightText)
                    }

                    // Ver detalle del acceso
                    IconButton(onClick = { onAccesoInfo(poi) }) {
                        Icon(Icons.Default.Info, null, tint = PrimaryOrange)
                    }

                    // Ir a este acceso
                    Button(
                        onClick = { onAccesoRoute(poi) },
                        shape   = RoundedCornerShape(10.dp),
                        colors  = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Icon(Icons.Default.Navigation, null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ir", fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(12.dp))
    }
}
