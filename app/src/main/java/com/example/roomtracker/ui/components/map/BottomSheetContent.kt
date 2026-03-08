package com.example.roomtracker.ui.components.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.roomtracker.ui.components.common.SearchField
import com.google.android.gms.maps.model.LatLng


@Composable
fun BottomSheetContent(
    pois: List<Pair<String, LatLng>>,
    search: String,
    onSearchChange: (String) -> Unit,
    onPlaceClick: (String) -> Unit,
    onRouteClick: (Pair<String, LatLng>) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        SearchField(
            value = search,
            onValueChange = onSearchChange,
            placeholder = "Buscar edificio, facultad, salón..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PUNTOS DE INTERÉS Y RUTAS",
            style = MaterialTheme.typography.labelMedium
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
        }
    }
}
