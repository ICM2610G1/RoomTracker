package com.example.roomtracker.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.components.common.SearchField
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.google.android.gms.maps.model.LatLng


@Composable
fun BottomSheetContent(
    pois: List<Pair<String, LatLng>>,
    search: String,
    onSearchChange: (String) -> Unit,
    onPlaceClick: (String) -> Unit,
    onRouteClick: (Pair<String, LatLng>) -> Unit,
    onNavigateToFood: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToOpportunities: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToCarnet: () -> Unit,
    onNavigateToStats: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // SERVICIOS (Estilo Uber) - FILA 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ServiceButton(
                icon = Icons.Default.Restaurant,
                label = "Comida",
                onClick = onNavigateToFood
            )
            ServiceButton(
                icon = Icons.Default.Event,
                label = "Eventos",
                onClick = onNavigateToEvents
            )
            ServiceButton(
                icon = Icons.Default.BusinessCenter,
                label = "Oportunidades",
                onClick = onNavigateToOpportunities
            )
            ServiceButton(
                icon = Icons.Default.Schedule,
                label = "Mi Horario",
                onClick = onNavigateToSchedule
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SERVICIOS - FILA 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            ServiceButton(
                icon = Icons.Default.ShoppingBag,
                label = "Tienda",
                onClick = onNavigateToShop
            )
            Spacer(modifier = Modifier.width(8.dp))
            ServiceButton(
                icon = Icons.Default.Badge,
                label = "Mi Carnet",
                onClick = onNavigateToCarnet
            )
            Spacer(modifier = Modifier.width(8.dp))
            ServiceButton(
                icon = Icons.Default.Analytics,
                label = "Estadisticas",
                onClick = onNavigateToStats
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SearchField(
            value = search,
            onValueChange = onSearchChange,
            placeholder = "Buscar edificio, facultad, salón..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PUNTOS DE INTERÉS Y RUTAS",
            style = MaterialTheme.typography.labelMedium,
            color = LightText,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        pois.forEach { poi ->
            PlaceItemCard(
                title = poi.first,
                onCardClick = {
                    onRouteClick(poi)
                },
                onInfoClick = {
                    onPlaceClick(poi.first)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ServiceButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryOrange,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
