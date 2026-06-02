package com.example.roomtracker.ui.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.viewmodel.FoodViewModel
import com.example.roomtracker.model.MenuItemJson
import java.text.NumberFormat
import java.util.*

private val priceFmt = NumberFormat.getNumberInstance(Locale("es", "CO"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodMenuScreen(
    onBack: () -> Unit,
    viewModel: FoodViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(40.dp))
        ScreenHeader(title = "Menú del Día", onBack = onBack)
        Spacer(Modifier.height(16.dp))

        when (val s = state) {

            is FoodViewModel.FoodState.Loading ->
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryOrange)
                }

            is FoodViewModel.FoodState.Error ->
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, color = Color.Gray, fontSize = 13.sp,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadMenu() }) { Text("Reintentar") }
                    }
                }

            is FoodViewModel.FoodState.Success -> {
                val allItems = s.items
                val filters = listOf("Todos") + allItems.map { it.categoria }.distinct()

                // Barra de búsqueda
                OutlinedTextField(
                    value       = search,
                    onValueChange = { search = it },
                    modifier    = Modifier.fillMaxWidth(),
                    shape       = RoundedCornerShape(16.dp),
                    placeholder = { Text("Buscar platos o restaurantes...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = LightText) },
                    singleLine  = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = PrimaryOrange,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor   = Color.White
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Filtros por categoría
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

                Spacer(Modifier.height(14.dp))
                Text("DISPONIBLES HOY", fontWeight = FontWeight.Bold,
                    color = LightText, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))

                val filtered = allItems.filter { item ->
                    (selectedFilter == "Todos" || item.categoria == selectedFilter) &&
                    (search.isBlank() || item.nombre.contains(search, ignoreCase = true) ||
                     item.lugar.contains(search, ignoreCase = true))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding      = PaddingValues(bottom = 24.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        FoodCard(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodCard(item: MenuItemJson) {
    var expanded by remember { mutableStateOf(false) }

    val categoriaColor = when (item.categoria) {
        "Almuerzo"  -> Color(0xFF1565C0)
        "Fast Food" -> Color(0xFFE65100)
        "Saludable" -> Color(0xFF2E7D32)
        "Postre"    -> Color(0xFF880E4F)
        else        -> PrimaryOrange
    }

    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Imagen con badges encima
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model              = item.imagenUrl,
                    contentDescription = item.nombre,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                // Badge categoría
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                    color    = categoriaColor,
                    shape    = RoundedCornerShape(8.dp)
                ) {
                    Text(item.categoria, color = Color.White, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                // Precio
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                    color    = Color.White,
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Text("\$${priceFmt.format(item.precio)}", color = PrimaryOrange,
                        fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(item.nombre, fontWeight = FontWeight.Bold,
                    fontSize = 17.sp, color = DarkText)

                Spacer(Modifier.height(6.dp))

                // Meta: lugar + tiempo + calorías
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (item.lugar.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null,
                                tint = PrimaryOrange, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(item.lugar, fontSize = 11.sp, color = LightText)
                        }
                    }
                    if (item.tiempoEntrega.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null,
                                tint = LightText, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(item.tiempoEntrega, fontSize = 11.sp, color = LightText)
                        }
                    }
                    if (item.calorias > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, null,
                                tint = Color(0xFFFF7043), modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("${item.calorias} cal", fontSize = 11.sp, color = LightText)
                        }
                    }
                }

                if (expanded) {
                    Spacer(Modifier.height(10.dp))
                    Text(item.descripcion, fontSize = 13.sp, color = DarkText, lineHeight = 19.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick  = { expanded = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PEDIR — \$${priceFmt.format(item.precio)}",
                            fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick  = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryOrange)
                    ) {
                        Text("VER DESCRIPCIÓN", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
