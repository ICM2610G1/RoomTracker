package com.example.roomtracker.ui.components.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.map.MapPoi
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange

@Composable
fun BottomSheetContent(
    pois: List<MapPoi>,
    search: String,
    onSearchChange: (String) -> Unit,
    onPlaceClick: (MapPoi) -> Unit,
    onGroupPlaceClick: (List<MapPoi>) -> Unit,
    onRouteClick: (MapPoi) -> Unit,
    onNavigateToFood: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToOpportunities: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToCarnet: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToForo: () -> Unit
) {
    val isSearching = search.isNotBlank()
    val focusManager = LocalFocusManager.current

    // Grupos: lista de listas agrupadas por displayName (deduplicado)
    val filteredGroups: List<List<MapPoi>> = if (isSearching) {
        pois.filter {
            it.displayName.contains(search, ignoreCase = true) ||
            it.nodeId.contains(search, ignoreCase = true) ||
            (it.info?.tipo?.contains(search, ignoreCase = true) == true)
        }.groupBy { it.displayName }.values.toList()
    } else {
        pois.groupBy { it.displayName }.values.toList()
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ─── Barra de búsqueda siempre visible ───────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value         = search,
                onValueChange = onSearchChange,
                modifier      = Modifier.weight(1f),
                shape         = RoundedCornerShape(16.dp),
                placeholder   = { Text("Buscar edificio, facultad, tipo...") },
                leadingIcon   = {
                    Icon(Icons.Default.Search, null, tint = if (isSearching) PrimaryOrange else LightText)
                },
                trailingIcon  = {
                    if (isSearching) {
                        IconButton(onClick = { onSearchChange(""); focusManager.clearFocus() }) {
                            Icon(Icons.Default.Close, null, tint = LightText)
                        }
                    }
                },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = PrimaryOrange,
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor   = Color.White
                )
            )
        }

        // ─── MODO BÚSQUEDA: solo lista de resultados ─────────────────────────
        AnimatedVisibility(visible = isSearching, enter = fadeIn(), exit = fadeOut()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header con contador
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        if (filteredGroups.isEmpty()) "Sin resultados"
                        else "${filteredGroups.size} lugar(es) encontrado(s)",
                        color      = if (filteredGroups.isEmpty()) Color.Gray else PrimaryOrange,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (filteredGroups.isNotEmpty()) {
                        Text("Toca ℹ️ para ver en mapa", color = LightText, fontSize = 10.sp)
                    }
                }

                Spacer(Modifier.height(4.dp))

                // LazyColumn de resultados agrupados
                LazyColumn(
                    modifier       = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredGroups.isEmpty()) {
                        item {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, null,
                                        tint     = Color.LightGray,
                                        modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("No se encontró \"$search\"",
                                        color = LightText, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(filteredGroups, key = { it.first().displayName }) { group ->
                            SearchResultItem(
                                group        = group,
                                onRouteClick = {
                                    onSearchChange("")
                                    focusManager.clearFocus()
                                    onRouteClick(group.first())
                                },
                                onInfoClick  = {
                                    onSearchChange("")
                                    focusManager.clearFocus()
                                    if (group.size == 1) onPlaceClick(group.first())
                                    else onGroupPlaceClick(group)
                                }
                            )
                        }
                    }
                }
            }
        }

        // ─── MODO NORMAL: servicios + lista de POIs cercanos ─────────────────
        AnimatedVisibility(visible = !isSearching, enter = fadeIn(), exit = fadeOut()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Servicios fila 1
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ServiceButton(Icons.Default.Restaurant,     "Comida",        onNavigateToFood)
                    ServiceButton(Icons.Default.Event,          "Eventos",       onNavigateToEvents)
                    ServiceButton(Icons.Default.BusinessCenter, "Oportunidades", onNavigateToOpportunities)
                    ServiceButton(Icons.Default.Schedule,       "Mi Horario",    onNavigateToSchedule)
                }

                Spacer(Modifier.height(12.dp))

                // Servicios fila 2
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ServiceButton(Icons.Default.ShoppingBag, "Tienda",        onNavigateToShop)
                    ServiceButton(Icons.Default.Badge,        "Mi Carnet",    onNavigateToCarnet)
                    ServiceButton(Icons.Default.Analytics,    "Estadísticas", onNavigateToStats)
                    ServiceButton(Icons.Default.Forum,        "Foro",         onNavigateToForo)
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Item de resultado de búsqueda ────────────────────────────────────────────

@Composable
fun SearchResultItem(
    group: List<MapPoi>,
    onRouteClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val poi   = group.first()
    val tipo  = poi.info?.tipo
    val icon  = tipoIcon(tipo)
    val color = tipoColor(tipo)
    val multipleAccesos = group.size > 1

    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono tipo
            Box(
                modifier         = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.12f),
                        androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(12.dp))

            // Nombre + badge de accesos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    poi.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    color      = DarkText
                )
                if (multipleAccesos) {
                    Spacer(Modifier.height(3.dp))
                    Surface(
                        color = color.copy(alpha = 0.10f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "${group.size} accesos · toca ℹ️ para elegir",
                            fontSize   = 9.sp,
                            color      = color,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        poi.nodeId,
                        fontSize = 9.sp,
                        color    = LightText.copy(alpha = 0.5f)
                    )
                }
            }

            // Botón ruta (va al primer nodo / más cercano)
            IconButton(onClick = onRouteClick, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.Navigation, null, tint = PrimaryOrange)
            }

            // Botón info: detalle si 1 nodo, picker si múltiples
            IconButton(onClick = onInfoClick, modifier = Modifier.size(38.dp)) {
                Icon(
                    if (multipleAccesos) Icons.Default.ForkLeft else Icons.Default.Info,
                    null,
                    tint = color
                )
            }
        }
    }
}

// ─── Botón de servicio ────────────────────────────────────────────────────────

@Composable
fun ServiceButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(60.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = PrimaryOrange, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text      = label,
            fontSize  = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color     = DarkText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
