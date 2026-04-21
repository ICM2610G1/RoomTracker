package com.example.roomtracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange

data class FoodItem(
    val name: String,
    val place: String,
    val price: String,
    val discount: String? = null,
    val time: String
)

@Composable
fun FoodMenuScreen(onBack: () -> Unit) {
    var search by remember { mutableStateOf("") }

    val dailyMenu = listOf(
        FoodItem("Bandeja Paisa", "Restaurante Central", "$15.000", "20% OFF", "15-20 min"),
        FoodItem("Ajiaco Santafereño", "Comedor Javeriano", "$12.500", null, "10-15 min"),
        FoodItem("Bowl Saludable", "Fresh Garden", "$18.900", "10% OFF", "20-25 min")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        ScreenHeader(title = "Menú del Día", onBack = onBack)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Buscar platos o restaurantes...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("OFERTAS DE HOY", fontWeight = FontWeight.Bold, color = LightText, fontSize = 12.sp)
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(dailyMenu) { item ->
                FoodCard(item)
            }
        }
    }
}

@Composable
fun FoodCard(item: FoodItem) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
                // Placeholder image background
                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
                
                item.discount?.let {
                    Surface(
                        color = PrimaryOrange,
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = it,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkText)
                    Text(item.price, fontWeight = FontWeight.Bold, color = PrimaryOrange)
                }
                Text(item.place, color = LightText, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🕒 ${item.time}", fontSize = 12.sp, color = LightText)
                }
            }
        }
    }
}
