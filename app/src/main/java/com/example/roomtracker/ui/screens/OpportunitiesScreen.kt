package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.FilterList
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

data class Opportunity(
    val title: String,
    val category: String,
    val organization: String,
    val deadline: String,
    val requirements: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunitiesScreen(onBack: () -> Unit) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Vacantes", "Monitorias", "Semilleros", "Misión")

    val opportunities = listOf(
        Opportunity("Monitoría Algoritmos", "Monitorias", "Depto. Ingeniería", "30 Ene", "Promedio > 4.0"),
        Opportunity("Desarrollador Junior", "Vacantes", "Centro Tecnológico", "15 Feb", "Conocimiento en Kotlin"),
        Opportunity("Semillero IA", "Semilleros", "Grupo de Investigación", "10 Feb", "Interés en Machine Learning"),
        Opportunity("Voluntariado Misión", "Misión", "Pastoral", "5 Feb", "Disponibilidad fines de semana")
    )

    val filteredOps = if (selectedFilter == "Todos") opportunities else opportunities.filter { it.category == selectedFilter }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        ScreenHeader(title = "Oportunidades", onBack = onBack)

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryOrange,
                        selectedLabelColor = Color.White,
                        labelColor = LightText
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("DISPONIBLES PARA TI", fontWeight = FontWeight.Bold, color = LightText, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(filteredOps) { op ->
                OpportunityCard(op)
            }
        }
    }
}

@Composable
fun OpportunityCard(op: Opportunity) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(PrimaryOrange.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = PrimaryOrange)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(op.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
                Text(op.organization, color = LightText, fontSize = 13.sp)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row {
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                        Text(op.category, color = Color(0xFF2E7D32), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Límite: ${op.deadline}", fontSize = 11.sp, color = LightText)
                }
            }
            
            IconButton(onClick = { /* Apply */ }) {
                Icon(Icons.Default.FilterList, contentDescription = "Ver más", tint = LightText)
            }
        }
    }
}
