package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange

data class ShopItem(
    val name: String,
    val category: String,
    val price: String,
    val stock: String,
    val deliveryTime: String
)

@Composable
fun VirtualShopScreen(onBack: () -> Unit) {
    var search by remember { mutableStateOf("") }

    val shopItems = listOf(
        ShopItem("Camiseta Javeriana", "Ropa", "$45.000", "Disponible", "1-2 días"),
        ShopItem("Cuaderno Argollado", "Papelería", "$12.500", "Pocas unidades", "Inmediato"),
        ShopItem("Termo Metálico", "Accesorios", "$35.000", "Disponible", "1 día")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        ScreenHeader(title = "Tienda Virtual", onBack = onBack)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Buscar productos...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("PRODUCTOS DESTACADOS", fontWeight = FontWeight.Bold, color = LightText, fontSize = 12.sp)
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(shopItems) { item ->
                ShopItemCard(item)
            }
        }
    }
}

@Composable
fun ShopItemCard(item: ShopItem) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(PrimaryOrange.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
                    Text(item.price, fontWeight = FontWeight.Bold, color = PrimaryOrange)
                }
                Text(item.category, color = LightText, fontSize = 13.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.stock, fontSize = 12.sp, color = if (item.stock == "Disponible") Color(0xFF2E7D32) else Color.Red)
                }
            }
        }
    }
}
