package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
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
import com.example.roomtracker.model.ProductoJson
import com.example.roomtracker.viewmodel.ShopViewModel
import java.text.NumberFormat
import java.util.Locale

private val shopFmt = NumberFormat.getNumberInstance(Locale("es", "CO"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualShopScreen(
    onBack: () -> Unit,
    viewModel: ShopViewModel,
    onNavigateToStore: () -> Unit = {}
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
        ScreenHeader(title = "Tienda Javeriana", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        // Banner de ubicación física
        Surface(
            onClick = onNavigateToStore,
            color   = PrimaryOrange.copy(alpha = 0.10f),
            shape   = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, null,
                    tint = PrimaryOrange, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tienda Javeriana",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryOrange)
                    Text("Toca para ver la ubicación en el mapa",
                        fontSize = 10.sp, color = LightText)
                }
                Icon(Icons.Default.ChevronRight, null, tint = PrimaryOrange)
            }
        }

        Spacer(Modifier.height(12.dp))

        when (val s = state) {

            is ShopViewModel.ShopState.Loading ->
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryOrange)
                }

            is ShopViewModel.ShopState.Error ->
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, color = Color.Gray, fontSize = 13.sp,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadProducts() }) { Text("Reintentar") }
                    }
                }

            is ShopViewModel.ShopState.Success -> {
                val allProductos = s.productos
                val filters = listOf("Todos") + allProductos.map { it.categoria }.distinct()

                // Búsqueda
                OutlinedTextField(
                    value       = search,
                    onValueChange = { search = it },
                    modifier    = Modifier.fillMaxWidth(),
                    shape       = RoundedCornerShape(16.dp),
                    placeholder = { Text("Buscar productos...") },
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

                // Filtros
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
                Text("PRODUCTOS OFICIALES", fontWeight = FontWeight.Bold,
                    color = LightText, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))

                val filtered = allProductos.filter { p ->
                    (selectedFilter == "Todos" || p.categoria == selectedFilter) &&
                    (search.isBlank() || p.nombre.contains(search, ignoreCase = true))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding      = PaddingValues(bottom = 24.dp)
                ) {
                    items(filtered, key = { it.id }) { producto ->
                        ProductCard(producto = producto)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(producto: ProductoJson) {
    var expanded by remember { mutableStateOf(false) }

    val categoriaColor = when (producto.categoria) {
        "Ropa"       -> Color(0xFF1565C0)
        "Accesorios" -> Color(0xFF6A1B9A)
        "Papelería"  -> Color(0xFF2E7D32)
        else         -> PrimaryOrange
    }

    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Imagen cuadrada al costado izquierdo
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
            ) {
                AsyncImage(
                    model              = producto.imagen_url,
                    contentDescription = producto.nombre,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                // Badge disponibilidad
                if (!producto.disponible) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AGOTADO", color = Color.White,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Info derecha
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                // Categoría
                Surface(
                    color = categoriaColor.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(producto.categoria, color = categoriaColor,
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }

                Spacer(Modifier.height(6.dp))
                Text(producto.nombre, fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, color = DarkText)

                Spacer(Modifier.height(4.dp))
                Text("\$${shopFmt.format(producto.precio)}", fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold, color = PrimaryOrange)

                if (expanded) {
                    Spacer(Modifier.height(6.dp))
                    Text(producto.descripcion, fontSize = 11.sp,
                        color = LightText, lineHeight = 16.sp)

                    if (producto.tallas.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Tallas disponibles:", fontSize = 11.sp,
                            color = DarkText, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(producto.tallas) { talla ->
                                Surface(
                                    shape    = RoundedCornerShape(8.dp),
                                    color    = Color.White,
                                    modifier = Modifier.border(
                                        1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)
                                    )
                                ) {
                                    Text(talla,
                                        fontSize   = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = DarkText,
                                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (!expanded) {
                    OutlinedButton(
                        onClick  = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryOrange)
                    ) {
                        Text("VER DETALLES", fontSize = 11.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick  = { expanded = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = LightText)
                    ) {
                        Text("CERRAR", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
